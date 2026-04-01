package com.libertymutual.audit.service;

import com.libertymutual.audit.model.AuditEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Cross-service enrichment — fetches related entity data from customer-service
 * and policy-service to provide full context in audit trail responses.
 */
@Service
public class EnrichmentService {

    private static final Logger log = LoggerFactory.getLogger(EnrichmentService.class);

    private final RestTemplate restTemplate;
    private final String customerServiceUrl;
    private final String policyServiceUrl;

    public EnrichmentService(
            RestTemplate restTemplate,
            @Value("${app.customer-service-url:http://localhost:3001}") String customerServiceUrl,
            @Value("${app.policy-service-url:http://localhost:3002}") String policyServiceUrl) {
        this.restTemplate = restTemplate;
        this.customerServiceUrl = customerServiceUrl;
        this.policyServiceUrl = policyServiceUrl;
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> enrichEvents(List<AuditEvent> events) {
        List<Map<String, Object>> enriched = new ArrayList<>();

        for (AuditEvent event : events) {
            Map<String, Object> enrichment = new HashMap<>();

            try {
                if ("customer".equals(event.getEntityType()) && event.getEntityId() != null) {
                    Map<String, Object> customer = restTemplate.getForObject(
                            customerServiceUrl + "/api/v1/customers/" + event.getEntityId(),
                            Map.class
                    );
                    if (customer != null) {
                        enrichment.put("customer", customer);
                    }
                }

                if ("policy".equals(event.getEntityType()) && event.getEntityId() != null) {
                    Map<String, Object> policy = restTemplate.getForObject(
                            policyServiceUrl + "/api/v1/policies/" + event.getEntityId(),
                            Map.class
                    );
                    if (policy != null) {
                        enrichment.put("policy", policy);
                    }
                }
            } catch (Exception e) {
                log.debug("Enrichment failed for event {}: {}", event.getId(), e.getMessage());
            }

            Map<String, Object> eventMap = new HashMap<>();
            eventMap.put("id", event.getId());
            eventMap.put("specversion", event.getSpecversion());
            eventMap.put("event_type", event.getEventType());
            eventMap.put("source", event.getSource());
            eventMap.put("event_time", event.getEventTime());
            eventMap.put("entity_id", event.getEntityId());
            eventMap.put("entity_type", event.getEntityType());
            eventMap.put("action", event.getAction());
            eventMap.put("actor", event.getActor());
            eventMap.put("changes", event.getChanges());
            eventMap.put("enrichment", enrichment);

            enriched.add(eventMap);
        }

        return enriched;
    }
}
