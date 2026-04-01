package com.yellowinsurance.claims.controller;

import com.yellowinsurance.claims.model.Customer;
import com.yellowinsurance.claims.model.dto.ApiResponse;
import com.yellowinsurance.claims.model.dto.CustomerDTO;
import com.yellowinsurance.claims.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/customers")
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Customer>>> getAllCustomers() {
        // VULNERABILITY: Returns all customer data including SSN and password hash
        List<Customer> customers = customerService.getAllCustomers();
        return ResponseEntity.ok(ApiResponse.ok(customers));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Customer>> getCustomerById(@PathVariable Long id) {
        // VULNERABILITY: No IDOR protection - any authenticated user can access any customer
        Optional<Customer> customer = customerService.getCustomerById(id);
        if (customer.isPresent()) {
            return ResponseEntity.ok(ApiResponse.ok(customer.get()));
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("Customer not found", "No customer with id: " + id));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Customer>> createCustomer(@RequestBody CustomerDTO dto) {
        // VULNERABILITY: Returns full customer object including SSN and password hash
        Customer customer = customerService.createCustomer(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(customer));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Customer>> updateCustomer(
            @PathVariable Long id, @RequestBody CustomerDTO dto) {
        Customer customer = customerService.updateCustomer(id, dto);
        return ResponseEntity.ok(ApiResponse.ok(customer));
    }

    /**
     * VULNERABILITY: SQL injection through search parameters
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<Customer>>> searchCustomers(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String state) {
        List<Customer> results = customerService.searchCustomers(name, email, state);
        return ResponseEntity.ok(ApiResponse.ok(results));
    }

    /**
     * VULNERABILITY: IDOR - exposes SSN lookup endpoint
     * No rate limiting, no authorization beyond basic auth
     */
    @GetMapping("/lookup/ssn/{ssn}")
    public ResponseEntity<ApiResponse<Customer>> lookupBySsn(@PathVariable String ssn) {
        Optional<Customer> customer = customerService.findBySsn(ssn);
        if (customer.isPresent()) {
            return ResponseEntity.ok(ApiResponse.ok(customer.get()));
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("Customer not found", "No customer with given SSN"));
    }

    /**
     * Deactivate a customer account.
     * ISSUE: No check for active policies or open claims
     */
    @PostMapping("/{id}/deactivate")
    public ResponseEntity<ApiResponse<Customer>> deactivateCustomer(@PathVariable Long id) {
        try {
            Customer customer = customerService.deactivateCustomer(id);
            return ResponseEntity.ok(ApiResponse.ok(customer));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to deactivate customer", e.getMessage()));
        }
    }

    /**
     * Delete a customer.
     * ISSUE: Hard delete, no cascading cleanup
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteCustomer(@PathVariable Long id) {
        try {
            customerService.deleteCustomer(id);
            return ResponseEntity.ok(ApiResponse.ok("Customer deleted"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Customer not found", e.getMessage()));
        }
    }

    /**
     * Process an address change for a customer.
     * ISSUE: No address validation, triggers risk score recalculation
     */
    @PostMapping("/{id}/address-change")
    public ResponseEntity<ApiResponse<Customer>> changeAddress(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        try {
            String street = request.get("street");
            String city = request.get("city");
            String state = request.get("state");
            String zip = request.get("zip");

            // ISSUE: No null checks on required fields
            Customer customer = customerService.changeAddress(id, street, city, state, zip);
            return ResponseEntity.ok(ApiResponse.ok(customer));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to change address", e.getMessage()));
        }
    }
}
