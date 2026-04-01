package com.libertymutual.audit.controller;

import com.libertymutual.audit.model.AuditEvent;
import com.libertymutual.audit.service.AuditEventService;
import com.libertymutual.audit.service.EnrichmentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuditEventsController.class)
class AuditEventsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuditEventService auditEventService;

    @MockBean
    private EnrichmentService enrichmentService;

    @Test
    void ingestEvent_validCloudEvent_returns201() throws Exception {
        UUID eventId = UUID.randomUUID();
        AuditEvent saved = new AuditEvent();
        saved.setId(eventId);
        saved.setSpecversion("1.0");
        saved.setEventType("customer.created");
        saved.setSource("/customer-service");
        saved.setEventTime(OffsetDateTime.now());
        saved.setCreatedAt(OffsetDateTime.now());
        saved.setRawEvent(new HashMap<>());

        when(auditEventService.ingestEvent(any(), any())).thenReturn(saved);

        String body = "{"
                + "\"specversion\":\"1.0\","
                + "\"type\":\"customer.created\","
                + "\"source\":\"/customer-service\","
                + "\"id\":\"" + eventId + "\","
                + "\"time\":\"" + OffsetDateTime.now() + "\","
                + "\"data\":{"
                + "\"entity_id\":\"" + UUID.randomUUID() + "\","
                + "\"entity_type\":\"customer\","
                + "\"action\":\"created\","
                + "\"actor\":\"system\""
                + "}}";

        mockMvc.perform(post("/api/v2/audit-events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("ingested"))
                .andExpect(jsonPath("$.event_id").value(eventId.toString()));
    }

    @Test
    void ingestEvent_missingRequiredFields_returns400() throws Exception {
        String body = "{"
                + "\"specversion\":\"1.0\","
                + "\"type\":\"customer.created\""
                + "}";

        mockMvc.perform(post("/api/v2/audit-events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("validation_error"));
    }

    @Test
    void ingestEvent_wrongSpecversion_returns400() throws Exception {
        String body = "{"
                + "\"specversion\":\"0.3\","
                + "\"type\":\"customer.created\","
                + "\"source\":\"/customer-service\","
                + "\"id\":\"" + UUID.randomUUID() + "\","
                + "\"time\":\"" + OffsetDateTime.now() + "\","
                + "\"data\":{}"
                + "}";

        mockMvc.perform(post("/api/v2/audit-events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Only CloudEvents specversion 1.0 is supported"));
    }
}
