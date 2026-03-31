package com.libertymutual.policy.service;

import com.libertymutual.policy.model.Policy;
import com.libertymutual.policy.model.ZipRiskData;
import com.libertymutual.policy.repository.PolicyRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class PolicyService {

    private static final BigDecimal BASE_RATE = new BigDecimal("0.01");

    private final PolicyRepository policyRepository;
    private final ZipRiskService zipRiskService;
    private final AuditClient auditClient;

    public PolicyService(PolicyRepository policyRepository, ZipRiskService zipRiskService, AuditClient auditClient) {
        this.policyRepository = policyRepository;
        this.zipRiskService = zipRiskService;
        this.auditClient = auditClient;
    }

    public List<Policy> listPolicies(UUID customerId, String type, String status) {
        if (customerId != null && type != null && status != null) {
            return policyRepository.findByCustomerIdAndTypeAndStatus(customerId, type, status);
        } else if (customerId != null && type != null) {
            return policyRepository.findByCustomerIdAndType(customerId, type);
        } else if (customerId != null && status != null) {
            return policyRepository.findByCustomerIdAndStatus(customerId, status);
        } else if (type != null && status != null) {
            return policyRepository.findByTypeAndStatus(type, status);
        } else if (customerId != null) {
            return policyRepository.findByCustomerId(customerId);
        } else if (type != null) {
            return policyRepository.findByType(type);
        } else if (status != null) {
            return policyRepository.findByStatus(status);
        }
        return policyRepository.findAll();
    }

    public Optional<Policy> getPolicyById(UUID id) {
        return policyRepository.findById(id);
    }

    public Policy createPolicy(UUID customerId, String type, BigDecimal coverageAmount, String zipCode) {
        Policy policy = new Policy();
        policy.setId(UUID.randomUUID());
        policy.setCustomerId(customerId);
        policy.setType(type);
        policy.setStatus("active");
        policy.setCoverageAmount(coverageAmount);
        policy.setZipCode(zipCode);
        policy.setEffectiveDate(LocalDate.now());
        policy.setExpiryDate(LocalDate.now().plusYears(1));
        policy.setCreatedAt(OffsetDateTime.now());
        policy.setUpdatedAt(OffsetDateTime.now());

        // Calculate initial premium based on ZIP risk factors
        BigDecimal premiumAnnual = coverageAmount.multiply(BASE_RATE);
        if (zipCode != null) {
            Optional<ZipRiskData> riskFactors = zipRiskService.getZipRiskFactors(zipCode);
            if (riskFactors.isPresent()) {
                premiumAnnual = premiumAnnual.multiply(BigDecimal.valueOf(riskFactors.get().getBaseModifier()));
            }
        }
        policy.setPremiumAnnual(premiumAnnual.setScale(2, RoundingMode.HALF_UP));

        Policy saved = policyRepository.save(policy);

        Map<String, Object> afterMap = new HashMap<>();
        afterMap.put("id", saved.getId().toString());
        afterMap.put("customer_id", saved.getCustomerId().toString());
        afterMap.put("type", saved.getType());
        afterMap.put("premium_annual", saved.getPremiumAnnual());
        afterMap.put("coverage_amount", saved.getCoverageAmount());

        auditClient.emitAuditEvent(
                "policy.created",
                "/policy-service",
                saved.getId().toString(),
                "policy",
                "created",
                null,
                afterMap
        );

        return saved;
    }

    public Map<String, Object> recalculatePremium(UUID policyId, String newZipcode, String oldZipcode) {
        Policy policy = policyRepository.findById(policyId)
                .orElseThrow(() -> new PolicyNotFoundException("Policy not found"));

        BigDecimal oldPremium = policy.getPremiumAnnual();
        String effectiveOldZip = oldZipcode != null ? oldZipcode : policy.getZipCode();

        Optional<ZipRiskData> riskFactorsOpt = zipRiskService.getZipRiskFactors(newZipcode);

        if (riskFactorsOpt.isEmpty()) {
            // Emit recalculation_failed audit event
            Map<String, Object> beforeMap = new HashMap<>();
            beforeMap.put("zip_code", effectiveOldZip);
            Map<String, Object> afterMap = new HashMap<>();
            afterMap.put("zip_code", newZipcode);
            afterMap.put("reason", "zip_risk_not_found");

            auditClient.emitAuditEvent(
                    "policy.recalculation_failed",
                    "/policy-service",
                    policyId.toString(),
                    "policy",
                    "recalculation_failed",
                    beforeMap,
                    afterMap
            );

            // Flag policy for manual review
            policy.setStatus("pending_review");
            policy.setUpdatedAt(OffsetDateTime.now());
            policyRepository.save(policy);

            Map<String, Object> result = new HashMap<>();
            result.put("policy_id", policyId.toString());
            result.put("old_premium", oldPremium);
            result.put("new_premium", oldPremium);
            result.put("risk_factors", null);
            result.put("recalculated_at", OffsetDateTime.now().toString());
            result.put("status", "pending_review");
            return result;
        }

        ZipRiskData riskFactors = riskFactorsOpt.get();

        // Calculate new premium
        BigDecimal basePremium = policy.getCoverageAmount().multiply(BASE_RATE);
        BigDecimal newPremium = basePremium.multiply(BigDecimal.valueOf(riskFactors.getBaseModifier()))
                .setScale(2, RoundingMode.HALF_UP);

        // Update policy
        policy.setPremiumAnnual(newPremium);
        policy.setZipCode(newZipcode);
        policy.setUpdatedAt(OffsetDateTime.now());
        policyRepository.save(policy);

        // Emit premium_recalculated audit event
        Map<String, Object> beforeMap = new HashMap<>();
        beforeMap.put("premium_annual", oldPremium);
        beforeMap.put("zip_code", effectiveOldZip);
        Map<String, Object> afterMap = new HashMap<>();
        afterMap.put("premium_annual", newPremium);
        afterMap.put("zip_code", newZipcode);

        auditClient.emitAuditEvent(
                "policy.premium_recalculated",
                "/policy-service",
                policyId.toString(),
                "policy",
                "premium_recalculated",
                beforeMap,
                afterMap
        );

        // Check if state requires coverage re-evaluation
        if (riskFactors.isRequiresCoverageReview()) {
            Map<String, Object> covBefore = new HashMap<>();
            covBefore.put("zip_code", effectiveOldZip);
            Map<String, Object> covAfter = new HashMap<>();
            covAfter.put("zip_code", newZipcode);
            covAfter.put("flood_zone", riskFactors.isFloodZone());
            covAfter.put("state", riskFactors.getState());

            auditClient.emitAuditEvent(
                    "policy.coverage_reevaluated",
                    "/policy-service",
                    policyId.toString(),
                    "policy",
                    "coverage_reevaluated",
                    covBefore,
                    covAfter
            );
        }

        Map<String, Object> result = new HashMap<>();
        result.put("policy_id", policyId.toString());
        result.put("old_premium", oldPremium);
        result.put("new_premium", newPremium);
        result.put("risk_factors", riskFactors);
        result.put("recalculated_at", OffsetDateTime.now().toString());
        return result;
    }

    public static class PolicyNotFoundException extends RuntimeException {
        public PolicyNotFoundException(String message) {
            super(message);
        }
    }
}
