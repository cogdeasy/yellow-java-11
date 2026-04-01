package com.libertymutual.customer.controller;

import com.libertymutual.customer.dto.AddressChangeResponse;
import com.libertymutual.customer.dto.AddressRequest;
import com.libertymutual.customer.dto.CreateCustomerRequest;
import com.libertymutual.customer.dto.ErrorResponse;
import com.libertymutual.customer.dto.ListResponse;
import com.libertymutual.customer.model.Customer;
import com.libertymutual.customer.service.CustomerService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/customers")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping
    public ResponseEntity<ListResponse<Customer>> listCustomers(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String city) {
        List<Customer> customers = customerService.listCustomers(status, state, city);
        return ResponseEntity.ok(new ListResponse<>(customers));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getCustomerById(@PathVariable UUID id) {
        return customerService.getCustomerById(id)
                .map(customer -> ResponseEntity.ok((Object) customer))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ErrorResponse("not_found", "Customer not found")));
    }

    @PostMapping
    public ResponseEntity<?> createCustomer(@Valid @RequestBody CreateCustomerRequest request) {
        Customer customer = customerService.createCustomer(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(customer);
    }

    @PatchMapping("/{id}/address")
    public ResponseEntity<?> changeAddress(
            @PathVariable UUID id,
            @Valid @RequestBody AddressRequest request) {
        try {
            AddressChangeResponse response = customerService.changeAddress(id, request);
            return ResponseEntity.ok(response);
        } catch (CustomerService.CustomerNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("not_found", e.getMessage()));
        }
    }
}
