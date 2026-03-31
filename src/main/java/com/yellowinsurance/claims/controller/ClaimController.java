package com.yellowinsurance.claims.controller;

import com.yellowinsurance.claims.model.Claim;
import com.yellowinsurance.claims.model.dto.ApiResponse;
import com.yellowinsurance.claims.model.dto.ClaimDTO;
import com.yellowinsurance.claims.service.ClaimService;
import com.yellowinsurance.claims.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/claims")
public class ClaimController {

    @Autowired
    private ClaimService claimService;

    @Autowired
    private NotificationService notificationService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Claim>>> getAllClaims() {
        List<Claim> claims = claimService.getAllClaims();
        return ResponseEntity.ok(ApiResponse.ok(claims));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Claim>> getClaimById(@PathVariable Long id) {
        Optional<Claim> claim = claimService.getClaimById(id);
        if (claim.isPresent()) {
            return ResponseEntity.ok(ApiResponse.ok(claim.get()));
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("Claim not found", "No claim with id: " + id));
    }

    // ISSUE: No @Valid annotation for request body validation
    @PostMapping
    public ResponseEntity<ApiResponse<Claim>> createClaim(@RequestBody ClaimDTO claimDTO) {
        Claim claim = claimService.createClaim(claimDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(claim));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<Claim>> updateStatus(
            @PathVariable Long id,
            @RequestParam String status,
            @RequestParam(required = false) String notes) {
        try {
            Claim claim = claimService.updateClaimStatus(id, status, notes);
            return ResponseEntity.ok(ApiResponse.ok(claim));
        } catch (Exception e) {
            // VULNERABILITY: Exposing stack trace to client
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            ApiResponse<Claim> response = ApiResponse.error("Failed to update claim", e.getMessage());
            response.setStackTrace(sw.toString());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * VULNERABILITY: SQL injection through search parameters
     * No input sanitization on any parameter
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<Claim>>> searchClaims(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortOrder) {
        List<Claim> results = claimService.searchClaims(q, status, sortBy, sortOrder);
        return ResponseEntity.ok(ApiResponse.ok(results));
    }

    /**
     * VULNERABILITY: SQL injection through report parameters
     * ISSUE: Business logic in controller
     */
    @GetMapping("/reports")
    public ResponseEntity<ApiResponse<List<Object[]>>> generateReport(
            @RequestParam String type,
            @RequestParam String startDate,
            @RequestParam String endDate,
            @RequestParam(defaultValue = "status") String groupBy) {
        List<Object[]> report = claimService.generateReport(type, startDate, endDate, groupBy);
        return ResponseEntity.ok(ApiResponse.ok(report));
    }

    @GetMapping("/statistics")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getStatistics() {
        return ResponseEntity.ok(ApiResponse.ok(claimService.getClaimStatistics()));
    }

    // VULNERABILITY: Exposing internal cache details - information disclosure
    @GetMapping("/cache")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getCacheStats() {
        return ResponseEntity.ok(ApiResponse.ok(claimService.getCacheStats()));
    }

    // ISSUE: No authorization check - anyone can clear the cache
    @DeleteMapping("/cache")
    public ResponseEntity<ApiResponse<String>> clearCache() {
        claimService.clearCache();
        return ResponseEntity.ok(ApiResponse.ok("Cache cleared"));
    }
}
