package com.yellowinsurance.claims.controller;

import com.yellowinsurance.claims.model.Policy;
import com.yellowinsurance.claims.model.dto.ApiResponse;
import com.yellowinsurance.claims.service.PolicyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/policies")
public class PolicyController {

    @Autowired
    private PolicyService policyService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Policy>>> getAllPolicies(
            @RequestParam(required = false) Long customerId) {
        List<Policy> policies;
        if (customerId != null) {
            policies = policyService.getPoliciesByCustomer(customerId);
        } else {
            policies = policyService.getAllPolicies();
        }
        return ResponseEntity.ok(ApiResponse.ok(policies));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Policy>> getPolicyById(@PathVariable Long id) {
        Optional<Policy> policy = policyService.getPolicyById(id);
        if (policy.isPresent()) {
            return ResponseEntity.ok(ApiResponse.ok(policy.get()));
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("Policy not found", "No policy with id: " + id));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Policy>> createPolicy(@RequestBody Policy policy) {
        // ISSUE: Accepting entity directly instead of DTO - mass assignment
        Policy created = policyService.createPolicy(policy);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(created));
    }

    @PostMapping("/{id}/recalculate")
    public ResponseEntity<ApiResponse<Map<String, Object>>> recalculatePremium(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        String newZipcode = request.get("new_zipcode");
        String oldZipcode = request.get("old_zipcode");
        Map<String, Object> result = policyService.recalculatePremium(id, newZipcode, oldZipcode);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @GetMapping("/zip-risk/{zipcode}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getZipRisk(@PathVariable String zipcode) {
        return ResponseEntity.ok(ApiResponse.ok(policyService.getZipRiskFactors(zipcode)));
    }
}
