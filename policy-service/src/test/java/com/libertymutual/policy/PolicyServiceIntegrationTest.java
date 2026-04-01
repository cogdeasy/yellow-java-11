package com.libertymutual.policy;

import com.libertymutual.policy.model.Policy;
import com.libertymutual.policy.repository.PolicyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class PolicyServiceIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("liberty_mutual_test")
            .withUsername("test")
            .withPassword("test")
            .withInitScript("schema.sql");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "none");
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PolicyRepository policyRepository;

    @BeforeEach
    void setUp() {
        policyRepository.deleteAll();
    }

    @Test
    void createAndRetrievePolicy() throws Exception {
        UUID customerId = UUID.randomUUID();
        String body = "{"
                + "\"customer_id\":\"" + customerId + "\","
                + "\"type\":\"home\","
                + "\"coverage_amount\":500000,"
                + "\"zip_code\":\"33101\""
                + "}";

        String response = mockMvc.perform(post("/api/v1/policies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.type").value("home"))
                .andExpect(jsonPath("$.premium_annual").value(8750.00))
                .andReturn().getResponse().getContentAsString();

        String id = com.fasterxml.jackson.databind.ObjectMapper
                .class.getDeclaredConstructor().newInstance()
                .readTree(response).get("id").asText();

        mockMvc.perform(get("/api/v1/policies/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("home"));
    }

    @Test
    void listPolicies_withCustomerFilter() throws Exception {
        UUID customerId = UUID.randomUUID();
        createTestPolicy(customerId, "auto", "02101", new BigDecimal("3000.00"));
        createTestPolicy(customerId, "home", "02101", new BigDecimal("5000.00"));
        createTestPolicy(UUID.randomUUID(), "auto", "33101", new BigDecimal("4000.00"));

        mockMvc.perform(get("/api/v1/policies").param("customer_id", customerId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(2));
    }

    @Test
    void recalculatePremium_knownZip_updatesPremium() throws Exception {
        UUID policyId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        Policy policy = new Policy();
        policy.setId(policyId);
        policy.setCustomerId(customerId);
        policy.setType("home");
        policy.setStatus("active");
        policy.setPremiumAnnual(new BigDecimal("5500.00"));
        policy.setCoverageAmount(new BigDecimal("500000.00"));
        policy.setZipCode("02101");
        policy.setEffectiveDate(LocalDate.now());
        policy.setExpiryDate(LocalDate.now().plusYears(1));
        policy.setCreatedAt(OffsetDateTime.now());
        policy.setUpdatedAt(OffsetDateTime.now());
        policyRepository.save(policy);

        String body = "{"
                + "\"new_zipcode\":\"33101\","
                + "\"old_zipcode\":\"02101\""
                + "}";

        mockMvc.perform(post("/api/v1/policies/" + policyId + "/recalculate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.old_premium").value(5500.00))
                .andExpect(jsonPath("$.new_premium").value(8750.00));
    }

    @Test
    void getZipRisk_knownZip_returnsRiskData() throws Exception {
        mockMvc.perform(get("/api/v1/zip-risk/33101"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.state").value("FL"))
                .andExpect(jsonPath("$.flood_zone").value(true));
    }

    @Test
    void getZipRisk_unknownZip_returns404() throws Exception {
        mockMvc.perform(get("/api/v1/zip-risk/99999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getPolicy_notFound_returns404() throws Exception {
        mockMvc.perform(get("/api/v1/policies/" + UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }

    @Test
    void healthCheck_returnsHealthy() throws Exception {
        mockMvc.perform(get("/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("healthy"))
                .andExpect(jsonPath("$.service").value("policy-service"));
    }

    private void createTestPolicy(UUID customerId, String type, String zipCode, BigDecimal premium) {
        Policy policy = new Policy();
        policy.setId(UUID.randomUUID());
        policy.setCustomerId(customerId);
        policy.setType(type);
        policy.setStatus("active");
        policy.setPremiumAnnual(premium);
        policy.setCoverageAmount(new BigDecimal("500000.00"));
        policy.setZipCode(zipCode);
        policy.setEffectiveDate(LocalDate.now());
        policy.setExpiryDate(LocalDate.now().plusYears(1));
        policy.setCreatedAt(OffsetDateTime.now());
        policy.setUpdatedAt(OffsetDateTime.now());
        policyRepository.save(policy);
    }
}
