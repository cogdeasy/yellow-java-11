package com.libertymutual.audit.controller;

import com.libertymutual.audit.dto.CloudEventRequest;
import com.libertymutual.audit.dto.ErrorResponse;
import com.libertymutual.audit.model.AuditEvent;
import com.libertymutual.audit.service.AuditEventService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v2/audit-events")
public class AuditEventsController {

    private final AuditEventService auditEventService;

    public AuditEventsController(AuditEventService auditEventService) {
        this.auditEventService = auditEventService;
    }

    @SuppressWarnings("unchecked")
    @PostMapping
    public ResponseEntity<?> ingestEvent(@RequestBody Map<String, Object> rawBody) {
        // Manual validation to match CloudEvents v1.0 spec
        String specversion = (String) rawBody.get("specversion");
        String type = (String) rawBody.get("type");
        String source = (String) rawBody.get("source");
        String id = (String) rawBody.get("id");
        String time = (String) rawBody.get("time");
        Object data = rawBody.get("data");

        StringBuilder missing = new StringBuilder();
        if (specversion == null || specversion.isBlank()) missing.append("specversion, ");
        if (type == null || type.isBlank()) missing.append("type, ");
        if (source == null || source.isBlank()) missing.append("source, ");
        if (id == null || id.isBlank()) missing.append("id, ");
        if (time == null || time.isBlank()) missing.append("time, ");
        if (data == null) missing.append("data, ");

        if (missing.length() > 0) {
            String missingFields = missing.substring(0, missing.length() - 2);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("validation_error",
                            "Missing required CloudEvents fields: " + missingFields));
        }

        if (!"1.0".equals(specversion)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("validation_error",
                            "Only CloudEvents specversion 1.0 is supported"));
        }

        try {
            UUID.fromString(id);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("validation_error",
                            "Field 'id' must be a valid UUID"));
        }

        try {
            OffsetDateTime.parse(time);
        } catch (DateTimeParseException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("validation_error",
                            "Field 'time' must be a valid ISO-8601 datetime"));
        }

        if (!(data instanceof Map)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("validation_error",
                            "Field 'data' must be a JSON object"));
        }

        CloudEventRequest request = new CloudEventRequest();
        request.setSpecversion(specversion);
        request.setType(type);
        request.setSource(source);
        request.setId(id);
        request.setTime(time);
        request.setData((Map<String, Object>) data);

        AuditEvent event = auditEventService.ingestEvent(request, rawBody);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", "ingested");
        response.put("event_id", event.getId().toString());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
