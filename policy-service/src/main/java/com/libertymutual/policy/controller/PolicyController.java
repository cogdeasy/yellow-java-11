package com.libertymutual.policy.controller;

import com.libertymutual.policy.dto.CreatePolicyRequest;
import com.libertymutual.policy.dto.ErrorResponse;
import com.libertymutual.policy.dto.ListResponse;
import com.libertymutual.policy.dto.RecalculateRequest;
import com.libertymutual.policy.model.Policy;
import com.libertymutual.policy.service.PolicyService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/policies")
public class PolicyController {

    private final PolicyService policyService;

    public PolicyController(PolicyService policyService) {
        this.policyService = policyService;
    }

    @GetMapping
    public ResponseEntity<ListResponse<Policy>> listPolicies(
            @RequestParam(name = "customer_id", required = false) UUID customerId,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status) {
        List<Policy> policies = policyService.listPolicies(customerId, type, status);
        return ResponseEntity.ok(new ListResponse<>(policies));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getPolicyById(@PathVariable UUID id) {
        return policyService.getPolicyById(id)
                .map(policy -> ResponseEntity.ok((Object) policy))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ErrorResponse("not_found", "Policy not found")));
    }

    @PostMapping
    public ResponseEntity<?> createPolicy(@Valid @RequestBody CreatePolicyRequest request) {
        Policy policy = policyService.createPolicy(
                request.getCustomerId(),
                request.getType(),
                request.getCoverageAmount(),
                request.getZipCode()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(policy);
    }

    @PostMapping("/{id}/recalculate")
    public ResponseEntity<?> recalculatePremium(
            @PathVariable UUID id,
            @Valid @RequestBody RecalculateRequest request) {
        try {
            Map<String, Object> result = policyService.recalculatePremium(
                    id, request.getNewZipcode(), request.getOldZipcode());
            return ResponseEntity.ok(result);
        } catch (PolicyService.PolicyNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("not_found", e.getMessage()));
        }
    }
}
