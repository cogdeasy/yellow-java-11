package com.libertymutual.policy.controller;

import com.libertymutual.policy.model.Policy;
import com.libertymutual.policy.service.PolicyService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PolicyController.class)
class PolicyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PolicyService policyService;

    @Test
    void listPolicies_returnsListResponse() throws Exception {
        Policy p1 = createPolicy("auto", "02101");
        Policy p2 = createPolicy("home", "33101");
        when(policyService.listPolicies(null, null, null))
                .thenReturn(Arrays.asList(p1, p2));

        mockMvc.perform(get("/api/v1/policies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(2))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void getPolicyById_found_returnsPolicy() throws Exception {
        UUID id = UUID.randomUUID();
        Policy policy = createPolicy("auto", "02101");
        policy.setId(id);
        when(policyService.getPolicyById(id)).thenReturn(Optional.of(policy));

        mockMvc.perform(get("/api/v1/policies/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("auto"));
    }

    @Test
    void getPolicyById_notFound_returns404() throws Exception {
        UUID id = UUID.randomUUID();
        when(policyService.getPolicyById(id)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/policies/" + id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("not_found"));
    }

    @Test
    void createPolicy_validRequest_returns201() throws Exception {
        Policy saved = createPolicy("home", "33101");
        when(policyService.createPolicy(any(), eq("home"), any(), eq("33101")))
                .thenReturn(saved);

        String body = "{"
                + "\"customer_id\":\"" + UUID.randomUUID() + "\","
                + "\"type\":\"home\","
                + "\"coverage_amount\":500000,"
                + "\"zip_code\":\"33101\""
                + "}";

        mockMvc.perform(post("/api/v1/policies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.type").value("home"));
    }

    @Test
    void recalculatePremium_success_returnsResult() throws Exception {
        UUID policyId = UUID.randomUUID();
        Map<String, Object> result = new HashMap<>();
        result.put("policy_id", policyId.toString());
        result.put("old_premium", new BigDecimal("5000.00"));
        result.put("new_premium", new BigDecimal("8750.00"));

        when(policyService.recalculatePremium(eq(policyId), eq("33101"), eq("02101")))
                .thenReturn(result);

        String body = "{"
                + "\"new_zipcode\":\"33101\","
                + "\"old_zipcode\":\"02101\""
                + "}";

        mockMvc.perform(post("/api/v1/policies/" + policyId + "/recalculate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.old_premium").value(5000.00))
                .andExpect(jsonPath("$.new_premium").value(8750.00));
    }

    @Test
    void recalculatePremium_policyNotFound_returns404() throws Exception {
        UUID policyId = UUID.randomUUID();
        when(policyService.recalculatePremium(eq(policyId), eq("33101"), eq("02101")))
                .thenThrow(new PolicyService.PolicyNotFoundException("Policy not found"));

        String body = "{"
                + "\"new_zipcode\":\"33101\","
                + "\"old_zipcode\":\"02101\""
                + "}";

        mockMvc.perform(post("/api/v1/policies/" + policyId + "/recalculate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("not_found"));
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
