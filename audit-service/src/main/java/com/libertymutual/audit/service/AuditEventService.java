package com.libertymutual.audit.service;

import com.libertymutual.audit.dto.AuditStatsResponse;
import com.libertymutual.audit.dto.CloudEventRequest;
import com.libertymutual.audit.model.AuditEvent;
import com.libertymutual.audit.repository.AuditEventRepository;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class AuditEventService {

    private final AuditEventRepository auditEventRepository;

    public AuditEventService(AuditEventRepository auditEventRepository) {
        this.auditEventRepository = auditEventRepository;
    }

    @SuppressWarnings("unchecked")
    public AuditEvent ingestEvent(CloudEventRequest request, Map<String, Object> rawBody) {
        AuditEvent event = new AuditEvent();
        event.setId(UUID.fromString(request.getId()));
        event.setSpecversion(request.getSpecversion());
        event.setEventType(request.getType());
        event.setSource(request.getSource());
        event.setEventTime(OffsetDateTime.parse(request.getTime()));
        event.setCreatedAt(OffsetDateTime.now());
        event.setRawEvent(rawBody);

        Map<String, Object> data = request.getData();
        if (data != null) {
            if (data.containsKey("entity_id") && data.get("entity_id") != null) {
                event.setEntityId(UUID.fromString((String) data.get("entity_id")));
            }
            event.setEntityType((String) data.get("entity_type"));
            event.setAction((String) data.get("action"));
            event.setActor((String) data.get("actor"));
            if (data.containsKey("changes")) {
                event.setChanges((Map<String, Object>) data.get("changes"));
            } else {
                event.setChanges(new HashMap<>());
            }
        }

        return auditEventRepository.save(event);
    }

    public List<AuditEvent> getAuditTrail(String entityType, UUID entityId,
                                           String source, OffsetDateTime from, OffsetDateTime to) {
        return auditEventRepository.findByFilters(entityType, entityId, source, from, to);
    }

    public List<AuditEvent> getEntityAuditTrail(String entityType, UUID entityId) {
        return auditEventRepository.findByEntityTypeAndEntityIdOrderByEventTimeAsc(entityType, entityId);
    }

    public AuditStatsResponse getStats() {
        AuditStatsResponse stats = new AuditStatsResponse();
        stats.setTotalEvents(auditEventRepository.count());

        Map<String, Long> eventsByType = new LinkedHashMap<>();
        for (Object[] row : auditEventRepository.countByEventType()) {
            eventsByType.put((String) row[0], (Long) row[1]);
        }
        stats.setEventsByType(eventsByType);

        Map<String, Long> eventsBySource = new LinkedHashMap<>();
        for (Object[] row : auditEventRepository.countBySource()) {
            eventsBySource.put((String) row[0], (Long) row[1]);
        }
        stats.setEventsBySource(eventsBySource);

        OffsetDateTime earliest = auditEventRepository.findEarliestEventTime();
        OffsetDateTime latest = auditEventRepository.findLatestEventTime();
        stats.setTimeRange(new AuditStatsResponse.TimeRange(
                earliest != null ? earliest.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME) : null,
                latest != null ? latest.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME) : null
        ));

        return stats;
    }
}
