package com.yellowinsurance.claims.controller;

import com.yellowinsurance.claims.model.AuditLog;
import com.yellowinsurance.claims.model.dto.ApiResponse;
import com.yellowinsurance.claims.repository.AuditLogRepository;
import com.yellowinsurance.claims.repository.ClaimRepository;
import com.yellowinsurance.claims.repository.CustomerRepository;
import com.yellowinsurance.claims.repository.PolicyRepository;
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
    private ClaimRepository claimRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private PolicyRepository policyRepository;

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

    /**
     * Dashboard/summary endpoint aggregating key metrics.
     * ISSUE: Loads ALL entities into memory just to count them
     * ISSUE: No caching - expensive query on every request
     * ISSUE: No authorization beyond basic auth
     */
    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDashboard() {
        Map<String, Object> dashboard = new HashMap<>();

        // ISSUE: Loading all entities into memory to get counts
        long totalClaims = claimRepository.findAll().size();
        long totalCustomers = customerRepository.findAll().size();
        long totalPolicies = policyRepository.findAll().size();

        long openClaims = claimRepository.findByStatus("OPEN").size();
        long underReviewClaims = claimRepository.findByStatus("UNDER_REVIEW").size();
        long approvedClaims = claimRepository.findByStatus("APPROVED").size();
        long deniedClaims = claimRepository.findByStatus("DENIED").size();

        long activePolicies = policyRepository.findByStatus("ACTIVE").size();
        long activeCustomers = customerRepository.findByStatus("ACTIVE").size();

        dashboard.put("totalClaims", totalClaims);
        dashboard.put("totalCustomers", totalCustomers);
        dashboard.put("totalPolicies", totalPolicies);
        dashboard.put("openClaims", openClaims);
        dashboard.put("underReviewClaims", underReviewClaims);
        dashboard.put("approvedClaims", approvedClaims);
        dashboard.put("deniedClaims", deniedClaims);
        dashboard.put("activePolicies", activePolicies);
        dashboard.put("activeCustomers", activeCustomers);
        dashboard.put("timestamp", System.currentTimeMillis());

        // VULNERABILITY: Exposing internal memory/runtime details
        dashboard.put("serverFreeMemory", Runtime.getRuntime().freeMemory());
        dashboard.put("serverUptime", System.currentTimeMillis());

        return ResponseEntity.ok(ApiResponse.ok(dashboard));
    }

    /**
     * Export a named report to a file.
     * VULNERABILITY: Path traversal - reportName is not sanitized
     * ISSUE: No content validation
     */
    @PostMapping("/reports/export")
    public ResponseEntity<ApiResponse<Map<String, String>>> exportReport(
            @RequestBody Map<String, String> request) {
        try {
            String reportName = request.get("reportName");
            String content = request.get("content");

            if (reportName == null || content == null) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Missing fields", "reportName and content are required"));
            }

            // Delegate to ReportService (which has path traversal vulnerability)
            String filePath = reportService.exportReport(reportName, content);

            Map<String, String> result = new HashMap<>();
            result.put("reportName", reportName);
            result.put("filePath", filePath); // VULNERABILITY: Exposing internal file path
            result.put("status", "exported");

            return ResponseEntity.ok(ApiResponse.ok(result));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("Export failed", e.getMessage()));
        }
    }
}
