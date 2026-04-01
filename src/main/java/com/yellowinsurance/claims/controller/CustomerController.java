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
}
