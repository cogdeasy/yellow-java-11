package com.libertymutual.audit.controller;

import com.libertymutual.audit.dto.AuditStatsResponse;
import com.libertymutual.audit.model.AuditEvent;
import com.libertymutual.audit.service.AuditEventService;
import com.libertymutual.audit.service.EnrichmentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuditTrailController.class)
class AuditTrailControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuditEventService auditEventService;

    @MockBean
    private EnrichmentService enrichmentService;

    @Test
    void getAuditTrail_returnsEnrichedEvents() throws Exception {
        AuditEvent event = createAuditEvent("customer.created");
        when(auditEventService.getAuditTrail(any(), any(), any(), any(), any()))
                .thenReturn(Collections.singletonList(event));

        Map<String, Object> enriched = new HashMap<>();
        enriched.put("id", event.getId());
        enriched.put("event_type", "customer.created");
        when(enrichmentService.enrichEvents(anyList()))
                .thenReturn(Collections.singletonList(enriched));

        mockMvc.perform(get("/api/v2/audit-trail"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.data[0].event_type").value("customer.created"));
    }

    @Test
    void getEntityAuditTrail_returnsEventsForEntity() throws Exception {
        UUID entityId = UUID.randomUUID();
        AuditEvent e1 = createAuditEvent("customer.created");
        AuditEvent e2 = createAuditEvent("customer.address_changed");

        when(auditEventService.getEntityAuditTrail("customer", entityId))
                .thenReturn(Arrays.asList(e1, e2));

        Map<String, Object> en1 = new HashMap<>();
        en1.put("event_type", "customer.created");
        Map<String, Object> en2 = new HashMap<>();
        en2.put("event_type", "customer.address_changed");

        when(enrichmentService.enrichEvents(anyList()))
                .thenReturn(Arrays.asList(en1, en2));

        mockMvc.perform(get("/api/v2/audit-trail/customer/" + entityId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(2));
    }

    @Test
    void getStats_returnsAggregatedStats() throws Exception {
        AuditStatsResponse stats = new AuditStatsResponse();
        stats.setTotalEvents(42);

        Map<String, Long> byType = new LinkedHashMap<>();
        byType.put("customer.created", 20L);
        byType.put("policy.created", 22L);
        stats.setEventsByType(byType);

        Map<String, Long> bySource = new LinkedHashMap<>();
        bySource.put("/customer-service", 20L);
        bySource.put("/policy-service", 22L);
        stats.setEventsBySource(bySource);

        stats.setTimeRange(new AuditStatsResponse.TimeRange(
                OffsetDateTime.now().minusDays(30).toString(),
                OffsetDateTime.now().toString()
        ));

        when(auditEventService.getStats()).thenReturn(stats);

        mockMvc.perform(get("/api/v2/audit-trail/summary/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total_events").value(42))
                .andExpect(jsonPath("$.events_by_type['customer.created']").value(20));
    }

    private AuditEvent createAuditEvent(String eventType) {
        AuditEvent event = new AuditEvent();
        event.setId(UUID.randomUUID());
        event.setSpecversion("1.0");
        event.setEventType(eventType);
        event.setSource("/customer-service");
        event.setEventTime(OffsetDateTime.now());
        event.setEntityId(UUID.randomUUID());
        event.setEntityType("customer");
        event.setAction("created");
        event.setActor("system");
        event.setChanges(new HashMap<>());
        event.setRawEvent(new HashMap<>());
        event.setCreatedAt(OffsetDateTime.now());
        return event;
    }
}
