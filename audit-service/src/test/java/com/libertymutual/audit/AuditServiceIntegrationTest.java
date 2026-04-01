package com.libertymutual.audit;

import com.libertymutual.audit.model.AuditEvent;
import com.libertymutual.audit.repository.AuditEventRepository;
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

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class AuditServiceIntegrationTest {

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
    private AuditEventRepository auditEventRepository;

    @BeforeEach
    void setUp() {
        auditEventRepository.deleteAll();
    }

    @Test
    void ingestAndRetrieveAuditEvent() throws Exception {
        UUID eventId = UUID.randomUUID();
        UUID entityId = UUID.randomUUID();

        String body = "{"
                + "\"specversion\":\"1.0\","
                + "\"type\":\"customer.created\","
                + "\"source\":\"/customer-service\","
                + "\"id\":\"" + eventId + "\","
                + "\"time\":\"" + OffsetDateTime.now() + "\","
                + "\"data\":{"
                + "\"entity_id\":\"" + entityId + "\","
                + "\"entity_type\":\"customer\","
                + "\"action\":\"created\","
                + "\"actor\":\"system\","
                + "\"changes\":{\"before\":null,\"after\":{\"first_name\":\"John\"}}"
                + "}}";

        mockMvc.perform(post("/api/v2/audit-events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("ingested"))
                .andExpect(jsonPath("$.event_id").value(eventId.toString()));

        // Retrieve via audit trail
        mockMvc.perform(get("/api/v2/audit-trail")
                        .param("entity_type", "customer"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(1));
    }

    @Test
    void getEntityAuditTrail() throws Exception {
        UUID entityId = UUID.randomUUID();

        // Ingest two events for the same entity
        ingestEvent("customer.created", entityId);
        ingestEvent("customer.address_changed", entityId);

        mockMvc.perform(get("/api/v2/audit-trail/customer/" + entityId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(2));
    }

    @Test
    void getStats_returnsAggregatedData() throws Exception {
        UUID entityId1 = UUID.randomUUID();
        UUID entityId2 = UUID.randomUUID();

        ingestEvent("customer.created", entityId1);
        ingestEvent("policy.created", entityId2);

        mockMvc.perform(get("/api/v2/audit-trail/summary/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total_events").value(2));
    }

    @Test
    void ingestEvent_missingFields_returns400() throws Exception {
        String body = "{\"specversion\":\"1.0\"}";

        mockMvc.perform(post("/api/v2/audit-events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("validation_error"));
    }

    @Test
    void healthCheck_returnsHealthy() throws Exception {
        mockMvc.perform(get("/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("healthy"))
                .andExpect(jsonPath("$.service").value("audit-service"));
    }

    private void ingestEvent(String type, UUID entityId) throws Exception {
        UUID eventId = UUID.randomUUID();
        String body = "{"
                + "\"specversion\":\"1.0\","
                + "\"type\":\"" + type + "\","
                + "\"source\":\"/customer-service\","
                + "\"id\":\"" + eventId + "\","
                + "\"time\":\"" + OffsetDateTime.now() + "\","
                + "\"data\":{"
                + "\"entity_id\":\"" + entityId + "\","
                + "\"entity_type\":\"customer\","
                + "\"action\":\"created\","
                + "\"actor\":\"system\""
                + "}}";

        mockMvc.perform(post("/api/v2/audit-events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());
    }
}
