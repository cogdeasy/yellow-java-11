package com.libertymutual.audit.controller;

import com.libertymutual.audit.dto.AuditStatsResponse;
import com.libertymutual.audit.dto.ListResponse;
import com.libertymutual.audit.model.AuditEvent;
import com.libertymutual.audit.service.AuditEventService;
import com.libertymutual.audit.service.EnrichmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v2/audit-trail")
public class AuditTrailController {

    private final AuditEventService auditEventService;
    private final EnrichmentService enrichmentService;

    public AuditTrailController(AuditEventService auditEventService, EnrichmentService enrichmentService) {
        this.auditEventService = auditEventService;
        this.enrichmentService = enrichmentService;
    }

    @GetMapping
    public ResponseEntity<ListResponse<Map<String, Object>>> getAuditTrail(
            @RequestParam(name = "entity_type", required = false) String entityType,
            @RequestParam(name = "entity_id", required = false) UUID entityId,
            @RequestParam(required = false) String source,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to) {

        OffsetDateTime fromTime = from != null ? OffsetDateTime.parse(from) : null;
        OffsetDateTime toTime = to != null ? OffsetDateTime.parse(to) : null;

        List<AuditEvent> events = auditEventService.getAuditTrail(entityType, entityId, source, fromTime, toTime);
        List<Map<String, Object>> enriched = enrichmentService.enrichEvents(events);
        return ResponseEntity.ok(new ListResponse<>(enriched));
    }

    @GetMapping("/summary/stats")
    public ResponseEntity<AuditStatsResponse> getStats() {
        return ResponseEntity.ok(auditEventService.getStats());
    }

    @GetMapping("/{type}/{id}")
    public ResponseEntity<ListResponse<Map<String, Object>>> getEntityAuditTrail(
            @PathVariable String type,
            @PathVariable UUID id) {
        List<AuditEvent> events = auditEventService.getEntityAuditTrail(type, id);
        List<Map<String, Object>> enriched = enrichmentService.enrichEvents(events);
        return ResponseEntity.ok(new ListResponse<>(enriched));
    }
}
