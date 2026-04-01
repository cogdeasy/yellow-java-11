package com.libertymutual.audit.service;

import com.libertymutual.audit.dto.AuditStatsResponse;
import com.libertymutual.audit.dto.CloudEventRequest;
import com.libertymutual.audit.model.AuditEvent;
import com.libertymutual.audit.repository.AuditEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuditEventServiceTest {

    @Mock
    private AuditEventRepository auditEventRepository;

    private AuditEventService auditEventService;

    @BeforeEach
    void setUp() {
        auditEventService = new AuditEventService(auditEventRepository);
    }

    @Test
    void ingestEvent_validCloudEvent_savesCorrectly() {
        String eventId = UUID.randomUUID().toString();
        CloudEventRequest request = new CloudEventRequest();
        request.setSpecversion("1.0");
        request.setType("customer.created");
        request.setSource("/customer-service");
        request.setId(eventId);
        request.setTime(OffsetDateTime.now().toString());

        Map<String, Object> data = new HashMap<>();
        data.put("entity_id", UUID.randomUUID().toString());
        data.put("entity_type", "customer");
        data.put("action", "created");
        data.put("actor", "system");
        Map<String, Object> changes = new HashMap<>();
        changes.put("before", null);
        changes.put("after", Map.of("first_name", "John"));
        data.put("changes", changes);
        request.setData(data);

        Map<String, Object> rawBody = new HashMap<>(data);
        rawBody.put("specversion", "1.0");
        rawBody.put("type", "customer.created");
        rawBody.put("source", "/customer-service");

        ArgumentCaptor<AuditEvent> captor = ArgumentCaptor.forClass(AuditEvent.class);
        when(auditEventRepository.save(captor.capture())).thenAnswer(inv -> inv.getArgument(0));

        AuditEvent result = auditEventService.ingestEvent(request, rawBody);

        assertNotNull(result);
        assertEquals("customer.created", result.getEventType());
        assertEquals("/customer-service", result.getSource());
        assertEquals("customer", result.getEntityType());
        assertEquals("created", result.getAction());
    }

    @Test
    void getAuditTrail_withFilters_delegatesToRepository() {
        OffsetDateTime from = OffsetDateTime.now().minusDays(7);
        OffsetDateTime to = OffsetDateTime.now();
        UUID entityId = UUID.randomUUID();

        AuditEvent event = createAuditEvent("customer.created");
        when(auditEventRepository.findByFilters("customer", entityId, null, from, to))
                .thenReturn(Collections.singletonList(event));

        List<AuditEvent> result = auditEventService.getAuditTrail("customer", entityId, null, from, to);

        assertEquals(1, result.size());
        verify(auditEventRepository).findByFilters("customer", entityId, null, from, to);
    }

    @Test
    void getEntityAuditTrail_returnsOrderedEvents() {
        UUID entityId = UUID.randomUUID();
        AuditEvent e1 = createAuditEvent("customer.created");
        AuditEvent e2 = createAuditEvent("customer.address_changed");

        when(auditEventRepository.findByEntityTypeAndEntityIdOrderByEventTimeAsc("customer", entityId))
                .thenReturn(Arrays.asList(e1, e2));

        List<AuditEvent> result = auditEventService.getEntityAuditTrail("customer", entityId);

        assertEquals(2, result.size());
    }

    @Test
    void getStats_returnsAggregatedData() {
        when(auditEventRepository.count()).thenReturn(42L);
        when(auditEventRepository.countByEventType())
                .thenReturn(Arrays.asList(
                        new Object[]{"customer.created", 20L},
                        new Object[]{"policy.created", 22L}
                ));
        when(auditEventRepository.countBySource())
                .thenReturn(Arrays.asList(
                        new Object[]{"/customer-service", 20L},
                        new Object[]{"/policy-service", 22L}
                ));
        when(auditEventRepository.findEarliestEventTime())
                .thenReturn(OffsetDateTime.now().minusDays(30));
        when(auditEventRepository.findLatestEventTime())
                .thenReturn(OffsetDateTime.now());

        AuditStatsResponse stats = auditEventService.getStats();

        assertEquals(42, stats.getTotalEvents());
        assertEquals(2, stats.getEventsByType().size());
        assertEquals(2, stats.getEventsBySource().size());
        assertNotNull(stats.getTimeRange());
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
