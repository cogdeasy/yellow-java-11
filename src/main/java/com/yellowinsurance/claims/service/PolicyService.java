package com.yellowinsurance.claims.service;

import com.yellowinsurance.claims.model.AuditLog;
import com.yellowinsurance.claims.model.Policy;
import com.yellowinsurance.claims.repository.AuditLogRepository;
import com.yellowinsurance.claims.repository.PolicyRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class PolicyService {

    private static final Logger logger = LogManager.getLogger(PolicyService.class);

    @Autowired
    private PolicyRepository policyRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    // ISSUE: Hardcoded risk factors - should be in database or config
    private static final Map<String, Double> ZIP_RISK_FACTORS = new HashMap<>();
    static {
        ZIP_RISK_FACTORS.put("33101", 1.45); // Miami, FL - hurricane
        ZIP_RISK_FACTORS.put("90210", 1.30); // Beverly Hills, CA - wildfire
        ZIP_RISK_FACTORS.put("77001", 1.25); // Houston, TX - flood
        ZIP_RISK_FACTORS.put("10001", 1.10); // NYC - moderate
        ZIP_RISK_FACTORS.put("80201", 1.05); // Denver, CO - low risk
        ZIP_RISK_FACTORS.put("02101", 1.08); // Boston, MA
        ZIP_RISK_FACTORS.put("70112", 1.40); // New Orleans, LA - hurricane/flood
        ZIP_RISK_FACTORS.put("85001", 1.15); // Phoenix, AZ
        ZIP_RISK_FACTORS.put("98101", 1.12); // Seattle, WA - earthquake
        ZIP_RISK_FACTORS.put("60601", 1.07); // Chicago, IL
    }

    // ISSUE: States requiring coverage re-evaluation (should be configurable)
    private static final List<String> MANDATORY_REVIEW_STATES = Arrays.asList("FL", "CA", "TX");

    public List<Policy> getAllPolicies() {
        return policyRepository.findAll();
    }

    public Optional<Policy> getPolicyById(Long id) {
        return policyRepository.findById(id);
    }

    public List<Policy> getPoliciesByCustomer(Long customerId) {
        return policyRepository.findByCustomerId(customerId);
    }

    public Policy createPolicy(Policy policy) {
        policy.setPolicyNumber(generatePolicyNumber(policy.getPolicyType()));
        policy.setStatus("ACTIVE");
        policy.setCreatedAt(LocalDateTime.now());
        policy.setUpdatedAt(LocalDateTime.now());

        // ISSUE: No validation of policy dates, coverage amount, etc.
        return policyRepository.save(policy);
    }

    /**
     * Recalculate premium based on new ZIP code
     * ISSUES:
     * - Floating point arithmetic for money (should use BigDecimal consistently)
     * - No audit trail for premium changes
     * - Magic numbers in calculation
     */
    public Map<String, Object> recalculatePremium(Long policyId, String newZipCode, String oldZipCode) {
        Policy policy = policyRepository.findById(policyId)
                .orElseThrow(() -> new RuntimeException("Policy not found: " + policyId));

        double oldRiskFactor = ZIP_RISK_FACTORS.getOrDefault(oldZipCode, 1.0);
        double newRiskFactor = ZIP_RISK_FACTORS.getOrDefault(newZipCode, 1.0);

        // ISSUE: Using double for financial calculations
        double currentPremium = policy.getPremium().doubleValue();
        double basePremium = currentPremium / oldRiskFactor;
        double newPremium = basePremium * newRiskFactor;

        // ISSUE: Rounding with floating point
        newPremium = Math.round(newPremium * 100.0) / 100.0;

        BigDecimal oldPremium = policy.getPremium();
        policy.setPremium(BigDecimal.valueOf(newPremium).setScale(2, RoundingMode.HALF_UP));
        policy.setUpdatedAt(LocalDateTime.now());

        policyRepository.save(policy);

        logger.info("Premium recalculated for policy " + policyId
                + ": " + oldPremium + " -> " + newPremium);

        Map<String, Object> result = new HashMap<>();
        result.put("policyId", policyId);
        result.put("oldPremium", oldPremium);
        result.put("newPremium", newPremium);
        result.put("oldRiskFactor", oldRiskFactor);
        result.put("newRiskFactor", newRiskFactor);
        result.put("changePercent", ((newPremium - currentPremium) / currentPremium) * 100);

        return result;
    }

    public Map<String, Object> getZipRiskFactors(String zipCode) {
        Map<String, Object> risk = new HashMap<>();
        risk.put("zipCode", zipCode);
        risk.put("riskFactor", ZIP_RISK_FACTORS.getOrDefault(zipCode, 1.0));
        risk.put("knownZip", ZIP_RISK_FACTORS.containsKey(zipCode));
        return risk;
    }

    public boolean requiresCoverageReview(String state) {
        return MANDATORY_REVIEW_STATES.contains(state.toUpperCase());
    }

    /**
     * Update a policy.
     * ISSUE: No audit trail for policy changes
     * ISSUE: Accepts entity directly - mass assignment vulnerability
     */
    public Policy updatePolicy(Long id, Policy updates) {
        Policy policy = policyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Policy not found: " + id));

        if (updates.getPolicyType() != null) policy.setPolicyType(updates.getPolicyType());
        if (updates.getCoverageAmount() != null) policy.setCoverageAmount(updates.getCoverageAmount());
        if (updates.getPremium() != null) policy.setPremium(updates.getPremium());
        if (updates.getDeductible() != null) policy.setDeductible(updates.getDeductible());
        if (updates.getStartDate() != null) policy.setStartDate(updates.getStartDate());
        if (updates.getEndDate() != null) policy.setEndDate(updates.getEndDate());
        if (updates.getRiskCategory() != null) policy.setRiskCategory(updates.getRiskCategory());
        if (updates.getUnderwriter() != null) policy.setUnderwriter(updates.getUnderwriter());

        policy.setUpdatedAt(LocalDateTime.now());

        logger.info("Policy " + id + " updated");
        Policy saved = policyRepository.save(policy);
        logAudit("POLICY", id, "UPDATED", "multiple fields", "updated");
        return saved;
    }

    /**
     * Cancel a policy.
     * ISSUE: No check for open claims on the policy
     * ISSUE: No refund calculation for remaining term
     */
    public Policy cancelPolicy(Long id, String reason) {
        Policy policy = policyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Policy not found: " + id));

        if ("CANCELLED".equals(policy.getStatus())) {
            throw new RuntimeException("Policy is already cancelled");
        }

        String oldStatus = policy.getStatus();
        policy.setStatus("CANCELLED");
        policy.setEndDate(LocalDate.now());
        policy.setUpdatedAt(LocalDateTime.now());

        logger.info("Policy " + id + " cancelled. Reason: " + reason + " (was " + oldStatus + ")");
        Policy saved = policyRepository.save(policy);
        logAudit("POLICY", id, "CANCELLED", oldStatus, "CANCELLED");
        return saved;
    }

    /**
     * Renew a policy for another year.
     * ISSUE: No premium adjustment for renewal
     * ISSUE: No check for outstanding claims or payment status
     * ISSUE: Floating-point arithmetic for premium increase
     */
    public Policy renewPolicy(Long id) {
        Policy policy = policyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Policy not found: " + id));

        if (!"ACTIVE".equals(policy.getStatus()) && !"EXPIRED".equals(policy.getStatus())) {
            throw new RuntimeException("Only ACTIVE or EXPIRED policies can be renewed");
        }

        // ISSUE: Hardcoded 3% increase, floating-point arithmetic for money
        double currentPremium = policy.getPremium().doubleValue();
        double newPremium = currentPremium * 1.03;
        policy.setPremium(BigDecimal.valueOf(newPremium).setScale(2, RoundingMode.HALF_UP));

        // Extend by one year from end date or today, whichever is later
        LocalDate newStart = policy.getEndDate() != null && policy.getEndDate().isAfter(LocalDate.now())
                ? policy.getEndDate() : LocalDate.now();
        policy.setStartDate(newStart);
        policy.setEndDate(newStart.plusYears(1));
        policy.setStatus("ACTIVE");
        policy.setUpdatedAt(LocalDateTime.now());

        String oldPremiumStr = String.valueOf(currentPremium);
        logger.info("Policy " + id + " renewed. New premium: " + newPremium);
        Policy saved = policyRepository.save(policy);
        logAudit("POLICY", id, "RENEWED", oldPremiumStr, String.valueOf(newPremium));
        return saved;
    }

    private void logAudit(String entityType, Long entityId, String action, String oldValue, String newValue) {
        try {
            AuditLog log = new AuditLog();
            log.setEntityType(entityType);
            log.setEntityId(entityId);
            log.setAction(action);
            log.setPerformedBy("system");
            log.setOldValue(oldValue);
            log.setNewValue(newValue);
            log.setCreatedAt(LocalDateTime.now());
            auditLogRepository.save(log);
        } catch (Exception e) {
            logger.error("Failed to create audit log", e);
        }
    }

    private String generatePolicyNumber(String type) {
        // ISSUE: Weak random, predictable policy numbers
        Random random = new Random();
        String prefix = "HOME";
        if ("AUTO".equalsIgnoreCase(type)) prefix = "AUTO";
        if ("LIFE".equalsIgnoreCase(type)) prefix = "LIFE";
        if ("HEALTH".equalsIgnoreCase(type)) prefix = "HLTH";
        return prefix + "-" + System.currentTimeMillis() + "-" + random.nextInt(9999);
    }
}
