package com.libertymutual.policy.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class AuditClient {

    private static final Logger log = LoggerFactory.getLogger(AuditClient.class);

    private final RestTemplate restTemplate;
    private final String auditServiceUrl;

    public AuditClient(
            RestTemplate restTemplate,
            @Value("${app.audit-service-url:http://localhost:3003}") String auditServiceUrl) {
        this.restTemplate = restTemplate;
        this.auditServiceUrl = auditServiceUrl;
    }

    public void emitAuditEvent(String type, String source, String entityId,
                               String entityType, String action,
                               Map<String, Object> before, Map<String, Object> after) {
        Map<String, Object> cloudEvent = new HashMap<>();
        cloudEvent.put("specversion", "1.0");
        cloudEvent.put("type", type);
        cloudEvent.put("source", source);
        cloudEvent.put("id", UUID.randomUUID().toString());
        cloudEvent.put("time", OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));

        Map<String, Object> data = new HashMap<>();
        data.put("entity_id", entityId);
        data.put("entity_type", entityType);
        data.put("action", action);
        data.put("actor", "system");

        Map<String, Object> changes = new HashMap<>();
        changes.put("before", before);
        changes.put("after", after);
        data.put("changes", changes);

        cloudEvent.put("data", data);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(cloudEvent, headers);
            restTemplate.postForEntity(auditServiceUrl + "/api/v2/audit-events", entity, String.class);
        } catch (Exception e) {
            log.error("Audit service unavailable, audit event not delivered: type={}, entity_id={}",
                    type, entityId, e);
        }
    }
}
