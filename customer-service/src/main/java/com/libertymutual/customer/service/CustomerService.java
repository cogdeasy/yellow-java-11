package com.libertymutual.customer.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.libertymutual.customer.dto.AddressChangeResponse;
import com.libertymutual.customer.dto.AddressRequest;
import com.libertymutual.customer.dto.CreateCustomerRequest;
import com.libertymutual.customer.model.Customer;
import com.libertymutual.customer.repository.CustomerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class CustomerService {

    private static final Logger log = LoggerFactory.getLogger(CustomerService.class);
    private static final List<String> STATES_REQUIRING_REVIEW = Arrays.asList("FL", "CA", "TX");

    private final CustomerRepository customerRepository;
    private final AuditClient auditClient;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final String policyServiceUrl;

    public CustomerService(
            CustomerRepository customerRepository,
            AuditClient auditClient,
            RestTemplate restTemplate,
            ObjectMapper objectMapper,
            @Value("${app.policy-service-url:http://localhost:3002}") String policyServiceUrl) {
        this.customerRepository = customerRepository;
        this.auditClient = auditClient;
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.policyServiceUrl = policyServiceUrl;
    }

    public List<Customer> listCustomers(String status, String state, String city) {
        if (status != null && state != null && city != null) {
            return customerRepository.findByStatusAndStateAndCity(status, state, city);
        } else if (status != null && state != null) {
            return customerRepository.findByStatusAndState(status, state);
        } else if (status != null && city != null) {
            return customerRepository.findByStatusAndCity(status, city);
        } else if (state != null && city != null) {
            return customerRepository.findByStateAndCity(state, city);
        } else if (status != null) {
            return customerRepository.findByStatus(status);
        } else if (state != null) {
            return customerRepository.findByState(state);
        } else if (city != null) {
            return customerRepository.findByCity(city);
        }
        return customerRepository.findAll();
    }

    public Optional<Customer> getCustomerById(UUID id) {
        return customerRepository.findById(id);
    }

    public Customer createCustomer(CreateCustomerRequest request) {
        Customer customer = new Customer();
        customer.setId(UUID.randomUUID());
        customer.setFirstName(request.getFirstName());
        customer.setLastName(request.getLastName());
        customer.setEmail(request.getEmail());
        customer.setPhone(request.getPhone());
        customer.setStreet(request.getAddress().getStreet());
        customer.setCity(request.getAddress().getCity());
        customer.setState(request.getAddress().getState());
        customer.setZip(request.getAddress().getZip());
        customer.setStatus("active");
        customer.setCreatedAt(OffsetDateTime.now());
        customer.setUpdatedAt(OffsetDateTime.now());

        Customer saved = customerRepository.save(customer);

        auditClient.emitAuditEvent(
                "customer.created",
                "/customer-service",
                saved.getId().toString(),
                "customer",
                "created",
                null,
                customerToMap(saved)
        );

        return saved;
    }

    @SuppressWarnings("unchecked")
    public AddressChangeResponse changeAddress(UUID customerId, AddressRequest request) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found"));

        Map<String, Object> oldAddress = new HashMap<>();
        oldAddress.put("street", customer.getStreet());
        oldAddress.put("city", customer.getCity());
        oldAddress.put("state", customer.getState());
        oldAddress.put("zip", customer.getZip());

        String oldZip = customer.getZip();

        customer.setStreet(request.getStreet());
        customer.setCity(request.getCity());
        customer.setState(request.getState());
        customer.setZip(request.getZip());
        customer.setUpdatedAt(OffsetDateTime.now());
        Customer updated = customerRepository.save(customer);

        Map<String, Object> newAddress = new HashMap<>();
        newAddress.put("street", request.getStreet());
        newAddress.put("city", request.getCity());
        newAddress.put("state", request.getState());
        newAddress.put("zip", request.getZip());

        auditClient.emitAuditEvent(
                "customer.address_changed",
                "/customer-service",
                customerId.toString(),
                "customer",
                "address_changed",
                oldAddress,
                newAddress
        );

        // Trigger premium recalculation via policy-service
        List<Map<String, Object>> premiumRecalculation = null;
        try {
            String policiesUrl = policyServiceUrl + "/api/v1/policies?customer_id=" + customerId;
            Map<String, Object> policiesResponse = restTemplate.getForObject(policiesUrl, Map.class);

            if (policiesResponse != null && policiesResponse.containsKey("data")) {
                List<Map<String, Object>> policies = (List<Map<String, Object>>) policiesResponse.get("data");
                if (policies != null && !policies.isEmpty()) {
                    premiumRecalculation = new ArrayList<>();
                    for (Map<String, Object> policy : policies) {
                        String policyId = (String) policy.get("id");
                        Map<String, String> recalcBody = new HashMap<>();
                        recalcBody.put("new_zipcode", request.getZip());
                        recalcBody.put("old_zipcode", oldZip);

                        try {
                            Map<String, Object> recalcResult = restTemplate.postForObject(
                                    policyServiceUrl + "/api/v1/policies/" + policyId + "/recalculate",
                                    recalcBody,
                                    Map.class
                            );
                            premiumRecalculation.add(recalcResult);
                        } catch (Exception e) {
                            log.warn("Failed to recalculate premium for policy {}: {}", policyId, e.getMessage());
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Policy service unavailable; address change still succeeds: {}", e.getMessage());
        }

        // Check if state requires mandatory coverage re-evaluation
        AddressChangeResponse.CoverageReevaluation coverageReevaluation = null;
        if (STATES_REQUIRING_REVIEW.contains(request.getState().toUpperCase())) {
            coverageReevaluation = new AddressChangeResponse.CoverageReevaluation(
                    true,
                    request.getState().toUpperCase(),
                    "pending_review"
            );
        }

        AddressChangeResponse response = new AddressChangeResponse();
        response.setCustomer(updated);
        response.setPremiumRecalculation(premiumRecalculation);
        response.setCoverageReevaluation(coverageReevaluation);
        return response;
    }

    private Map<String, Object> customerToMap(Customer customer) {
        return objectMapper.convertValue(customer, Map.class);
    }

    public static class CustomerNotFoundException extends RuntimeException {
        public CustomerNotFoundException(String message) {
            super(message);
        }
    }
}
