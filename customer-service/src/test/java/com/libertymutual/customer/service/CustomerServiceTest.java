package com.libertymutual.customer.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.libertymutual.customer.dto.AddressChangeResponse;
import com.libertymutual.customer.dto.AddressRequest;
import com.libertymutual.customer.dto.CreateCustomerRequest;
import com.libertymutual.customer.model.Customer;
import com.libertymutual.customer.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private AuditClient auditClient;

    @Mock
    private RestTemplate restTemplate;

    private CustomerService customerService;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        customerService = new CustomerService(
                customerRepository, auditClient, restTemplate, objectMapper,
                "http://localhost:3002");
    }

    @Test
    void listCustomers_noFilters_returnsAll() {
        Customer c1 = createCustomer("John", "Doe", "MA");
        Customer c2 = createCustomer("Jane", "Smith", "FL");
        when(customerRepository.findAll()).thenReturn(Arrays.asList(c1, c2));

        List<Customer> result = customerService.listCustomers(null, null, null);

        assertEquals(2, result.size());
    }

    @Test
    void listCustomers_byStatus_filtersCorrectly() {
        Customer c1 = createCustomer("John", "Doe", "MA");
        when(customerRepository.findByStatus("active")).thenReturn(Collections.singletonList(c1));

        List<Customer> result = customerService.listCustomers("active", null, null);

        assertEquals(1, result.size());
        assertEquals("John", result.get(0).getFirstName());
    }

    @Test
    void listCustomers_byState_filtersCorrectly() {
        Customer c1 = createCustomer("John", "Doe", "FL");
        when(customerRepository.findByState("FL")).thenReturn(Collections.singletonList(c1));

        List<Customer> result = customerService.listCustomers(null, "FL", null);

        assertEquals(1, result.size());
    }

    @Test
    void listCustomers_byStatusAndState_filtersCorrectly() {
        Customer c1 = createCustomer("John", "Doe", "FL");
        when(customerRepository.findByStatusAndState("active", "FL"))
                .thenReturn(Collections.singletonList(c1));

        List<Customer> result = customerService.listCustomers("active", "FL", null);

        assertEquals(1, result.size());
    }

    @Test
    void getCustomerById_exists_returnsCustomer() {
        UUID id = UUID.randomUUID();
        Customer customer = createCustomer("John", "Doe", "MA");
        customer.setId(id);
        when(customerRepository.findById(id)).thenReturn(Optional.of(customer));

        Optional<Customer> result = customerService.getCustomerById(id);

        assertTrue(result.isPresent());
        assertEquals("John", result.get().getFirstName());
    }

    @Test
    void getCustomerById_notFound_returnsEmpty() {
        UUID id = UUID.randomUUID();
        when(customerRepository.findById(id)).thenReturn(Optional.empty());

        Optional<Customer> result = customerService.getCustomerById(id);

        assertFalse(result.isPresent());
    }

    @Test
    void createCustomer_success_savesAndEmitsAudit() {
        CreateCustomerRequest request = new CreateCustomerRequest();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setEmail("john@example.com");
        request.setPhone("555-0100");

        AddressRequest address = new AddressRequest();
        address.setStreet("123 Main St");
        address.setCity("Boston");
        address.setState("MA");
        address.setZip("02101");
        request.setAddress(address);

        when(customerRepository.save(any(Customer.class))).thenAnswer(inv -> inv.getArgument(0));

        Customer result = customerService.createCustomer(request);

        assertNotNull(result);

        verify(auditClient).emitAuditEvent(
                eq("customer.created"), eq("/customer-service"),
                anyString(), eq("customer"), eq("created"), any(), any());

        assertEquals("John", result.getFirstName());
        assertEquals("Doe", result.getLastName());
        assertEquals("MA", result.getState());
        assertEquals("02101", result.getZip());
        assertEquals("active", result.getStatus());
    }

    @Test
    void changeAddress_customerNotFound_throwsException() {
        UUID id = UUID.randomUUID();
        AddressRequest address = new AddressRequest();
        address.setStreet("456 Oak Ave");
        address.setCity("Miami");
        address.setState("FL");
        address.setZip("33101");

        when(customerRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(CustomerService.CustomerNotFoundException.class,
                () -> customerService.changeAddress(id, address));
    }

    @Test
    void changeAddress_success_updatesAndEmitsAudit() {
        UUID id = UUID.randomUUID();
        Customer existing = createCustomer("John", "Doe", "MA");
        existing.setId(id);
        existing.setStreet("123 Main St");
        existing.setCity("Boston");
        existing.setZip("02101");

        AddressRequest newAddr = new AddressRequest();
        newAddr.setStreet("456 Oak Ave");
        newAddr.setCity("Cambridge");
        newAddr.setState("MA");
        newAddr.setZip("02102");

        when(customerRepository.findById(id)).thenReturn(Optional.of(existing));
        when(customerRepository.save(any(Customer.class))).thenAnswer(inv -> inv.getArgument(0));

        // Mock policy-service response - returns a Map since RestTemplate deserializes JSON
        Map<String, Object> policiesResponse = new HashMap<>();
        policiesResponse.put("data", Collections.emptyList());
        policiesResponse.put("total", 0);
        when(restTemplate.getForObject(anyString(), eq(Map.class))).thenReturn(policiesResponse);

        AddressChangeResponse result = customerService.changeAddress(id, newAddr);

        assertNotNull(result);
        assertEquals("456 Oak Ave", result.getCustomer().getStreet());
        assertEquals("Cambridge", result.getCustomer().getCity());
        // MA is not in STATES_REQUIRING_REVIEW, so coverageReevaluation.triggered is false
        assertNotNull(result.getCoverageReevaluation());
        assertFalse(result.getCoverageReevaluation().isTriggered());
        assertEquals("not_required", result.getCoverageReevaluation().getResult());
    }

    @Test
    void changeAddress_toFloridaZip_triggersCoverageReevaluation() {
        UUID id = UUID.randomUUID();
        Customer existing = createCustomer("John", "Doe", "MA");
        existing.setId(id);
        existing.setStreet("123 Main St");
        existing.setCity("Boston");
        existing.setZip("02101");

        AddressRequest newAddr = new AddressRequest();
        newAddr.setStreet("789 Beach Blvd");
        newAddr.setCity("Miami");
        newAddr.setState("FL");
        newAddr.setZip("33101");

        when(customerRepository.findById(id)).thenReturn(Optional.of(existing));
        when(customerRepository.save(any(Customer.class))).thenAnswer(inv -> inv.getArgument(0));

        Map<String, Object> policiesResponse = new HashMap<>();
        policiesResponse.put("data", Collections.emptyList());
        policiesResponse.put("total", 0);
        when(restTemplate.getForObject(anyString(), eq(Map.class))).thenReturn(policiesResponse);

        AddressChangeResponse result = customerService.changeAddress(id, newAddr);

        assertNotNull(result);
        assertTrue(result.getCoverageReevaluation().isTriggered());
        assertEquals("FL", result.getCoverageReevaluation().getState());
    }

    private Customer createCustomer(String firstName, String lastName, String state) {
        Customer customer = new Customer();
        customer.setId(UUID.randomUUID());
        customer.setFirstName(firstName);
        customer.setLastName(lastName);
        customer.setEmail(firstName.toLowerCase() + "@example.com");
        customer.setPhone("555-0100");
        customer.setStreet("123 Main St");
        customer.setCity("Boston");
        customer.setState(state);
        customer.setZip("02101");
        customer.setStatus("active");
        customer.setCreatedAt(OffsetDateTime.now());
        customer.setUpdatedAt(OffsetDateTime.now());
        return customer;
    }
}
