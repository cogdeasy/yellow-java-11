package com.yellowinsurance.claims.controller;

import com.yellowinsurance.claims.model.AuditLog;
import com.yellowinsurance.claims.model.dto.ApiResponse;
import com.yellowinsurance.claims.repository.AuditLogRepository;
import com.yellowinsurance.claims.service.ReportService;
import com.yellowinsurance.claims.util.XmlParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private ReportService reportService;

    @Autowired
    private XmlParser xmlParser;

    /**
     * VULNERABILITY: No admin-specific authorization
     * Anyone with basic auth can access audit logs
     */
    @GetMapping("/audit-logs")
    public ResponseEntity<ApiResponse<List<AuditLog>>> getAuditLogs(
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) Long entityId) {
        List<AuditLog> logs;
        if (entityType != null && entityId != null) {
            logs = auditLogRepository.findByEntityTypeAndEntityId(entityType, entityId);
        } else {
            logs = auditLogRepository.findAll();
        }
        return ResponseEntity.ok(ApiResponse.ok(logs));
    }

    @GetMapping("/reports/claims-csv")
    public ResponseEntity<byte[]> exportClaimsCsv(
            @RequestParam(required = false) String status) {
        String csv = reportService.generateClaimsCsv(status);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv"));
        headers.setContentDispositionFormData("attachment", "claims-report.csv");
        return ResponseEntity.ok().headers(headers).body(csv.getBytes());
    }

    /**
     * VULNERABILITY: XXE - parses XML without disabling external entities
     */
    @PostMapping("/import/xml")
    public ResponseEntity<ApiResponse<Map<String, Object>>> importXml(@RequestBody String xmlContent) {
        Map<String, Object> result = xmlParser.parseXml(xmlContent);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    /**
     * VULNERABILITY: Information disclosure - exposes system properties
     */
    @GetMapping("/system-info")
    public ResponseEntity<ApiResponse<Map<String, String>>> getSystemInfo() {
        Map<String, String> info = new HashMap<>();
        info.put("java.version", System.getProperty("java.version"));
        info.put("os.name", System.getProperty("os.name"));
        info.put("os.version", System.getProperty("os.version"));
        info.put("user.dir", System.getProperty("user.dir"));
        info.put("user.home", System.getProperty("user.home"));
        info.put("java.class.path", System.getProperty("java.class.path"));
        // VULNERABILITY: Exposing environment variables
        info.put("PATH", System.getenv("PATH") != null ? System.getenv("PATH") : "N/A");
        info.put("JAVA_HOME", System.getenv("JAVA_HOME") != null ? System.getenv("JAVA_HOME") : "N/A");
        return ResponseEntity.ok(ApiResponse.ok(info));
    }

    @GetMapping("/health")
    public ResponseEntity<ApiResponse<Map<String, Object>>> healthCheck() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("timestamp", System.currentTimeMillis());
        health.put("freeMemory", Runtime.getRuntime().freeMemory());
        health.put("totalMemory", Runtime.getRuntime().totalMemory());
        health.put("maxMemory", Runtime.getRuntime().maxMemory());
        return ResponseEntity.ok(ApiResponse.ok(health));
    }
}
