package com.libertymutual.policy.service;

import com.libertymutual.policy.model.Policy;
import com.libertymutual.policy.repository.PolicyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PolicyServiceTest {

    @Mock
    private PolicyRepository policyRepository;

    @Mock
    private ZipRiskService zipRiskService;

    @Mock
    private AuditClient auditClient;

    private PolicyService policyService;

    @BeforeEach
    void setUp() {
        policyService = new PolicyService(policyRepository, zipRiskService, auditClient);
    }

    @Test
    void listPolicies_noFilters_returnsAll() {
        Policy p1 = createPolicy("auto", "02101");
        Policy p2 = createPolicy("home", "33101");
        when(policyRepository.findAll()).thenReturn(Arrays.asList(p1, p2));

        List<Policy> result = policyService.listPolicies(null, null, null);

        assertEquals(2, result.size());
    }

    @Test
    void listPolicies_byCustomerId_filtersCorrectly() {
        UUID customerId = UUID.randomUUID();
        Policy p1 = createPolicy("auto", "02101");
        when(policyRepository.findByCustomerId(customerId))
                .thenReturn(Collections.singletonList(p1));

        List<Policy> result = policyService.listPolicies(customerId, null, null);

        assertEquals(1, result.size());
    }

    @Test
    void getPolicyById_exists_returnsPolicy() {
        UUID id = UUID.randomUUID();
        Policy policy = createPolicy("auto", "02101");
        policy.setId(id);
        when(policyRepository.findById(id)).thenReturn(Optional.of(policy));

        Optional<Policy> result = policyService.getPolicyById(id);

        assertTrue(result.isPresent());
    }

    @Test
    void createPolicy_withKnownZip_appliesRiskModifier() {
        UUID customerId = UUID.randomUUID();
        com.libertymutual.policy.model.ZipRiskData riskData =
                new com.libertymutual.policy.model.ZipRiskData(
                        "33101", true, 0.85, 0.1, 0.5, 1.75, "FL", true);
        when(zipRiskService.getZipRiskFactors("33101")).thenReturn(Optional.of(riskData));
        when(policyRepository.save(any(Policy.class))).thenAnswer(inv -> inv.getArgument(0));
        doNothing().when(auditClient).emitAuditEvent(
                anyString(), anyString(), anyString(), anyString(), anyString(), any(), any());

        Policy result = policyService.createPolicy(
                customerId, "home", new BigDecimal("500000"), "33101");

        assertNotNull(result);
        // base = 500000 * 0.01 = 5000, modified = 5000 * 1.75 = 8750.00
        assertEquals(new BigDecimal("8750.00"), result.getPremiumAnnual());
    }

    @Test
    void createPolicy_withUnknownZip_usesBaseRate() {
        UUID customerId = UUID.randomUUID();
        when(zipRiskService.getZipRiskFactors("99999")).thenReturn(Optional.empty());
        when(policyRepository.save(any(Policy.class))).thenAnswer(inv -> inv.getArgument(0));
        doNothing().when(auditClient).emitAuditEvent(
                anyString(), anyString(), anyString(), anyString(), anyString(), any(), any());

        Policy result = policyService.createPolicy(
                customerId, "auto", new BigDecimal("300000"), "99999");

        assertNotNull(result);
        // base = 300000 * 0.01 = 3000.00, no modifier
        assertEquals(new BigDecimal("3000.00"), result.getPremiumAnnual());
    }

    @Test
    void recalculatePremium_policyNotFound_throwsException() {
        UUID policyId = UUID.randomUUID();
        when(policyRepository.findById(policyId)).thenReturn(Optional.empty());

        assertThrows(PolicyService.PolicyNotFoundException.class,
                () -> policyService.recalculatePremium(policyId, "33101", "02101"));
    }

    @Test
    void recalculatePremium_knownZip_updatesPolicy() {
        UUID policyId = UUID.randomUUID();
        Policy policy = createPolicy("home", "02101");
        policy.setId(policyId);
        policy.setPremiumAnnual(new BigDecimal("5500.00"));
        policy.setCoverageAmount(new BigDecimal("500000"));

        com.libertymutual.policy.model.ZipRiskData riskData =
                new com.libertymutual.policy.model.ZipRiskData(
                        "33101", true, 0.85, 0.1, 0.5, 1.75, "FL", true);

        when(policyRepository.findById(policyId)).thenReturn(Optional.of(policy));
        when(zipRiskService.getZipRiskFactors("33101")).thenReturn(Optional.of(riskData));
        when(policyRepository.save(any(Policy.class))).thenAnswer(inv -> inv.getArgument(0));
        doNothing().when(auditClient).emitAuditEvent(
                anyString(), anyString(), anyString(), anyString(), anyString(), any(), any());

        Map<String, Object> result = policyService.recalculatePremium(policyId, "33101", "02101");

        assertNotNull(result);
        assertEquals(new BigDecimal("5500.00"), result.get("old_premium"));
        assertEquals(new BigDecimal("8750.00"), result.get("new_premium"));
    }

    @Test
    void recalculatePremium_unknownZip_setsPendingReview() {
        UUID policyId = UUID.randomUUID();
        Policy policy = createPolicy("auto", "02101");
        policy.setId(policyId);
        policy.setPremiumAnnual(new BigDecimal("3000.00"));
        policy.setCoverageAmount(new BigDecimal("300000"));

        when(policyRepository.findById(policyId)).thenReturn(Optional.of(policy));
        when(zipRiskService.getZipRiskFactors("99999")).thenReturn(Optional.empty());
        when(policyRepository.save(any(Policy.class))).thenAnswer(inv -> inv.getArgument(0));
        doNothing().when(auditClient).emitAuditEvent(
                anyString(), anyString(), anyString(), anyString(), anyString(), any(), any());

        Map<String, Object> result = policyService.recalculatePremium(policyId, "99999", "02101");

        assertNotNull(result);
        assertEquals("pending_review", result.get("status"));
    }

    private Policy createPolicy(String type, String zipCode) {
        Policy policy = new Policy();
        policy.setId(UUID.randomUUID());
        policy.setCustomerId(UUID.randomUUID());
        policy.setType(type);
        policy.setStatus("active");
        policy.setPremiumAnnual(new BigDecimal("5000.00"));
        policy.setCoverageAmount(new BigDecimal("500000.00"));
        policy.setZipCode(zipCode);
        policy.setEffectiveDate(LocalDate.now());
        policy.setExpiryDate(LocalDate.now().plusYears(1));
        policy.setCreatedAt(OffsetDateTime.now());
        policy.setUpdatedAt(OffsetDateTime.now());
        return policy;
    }
}
