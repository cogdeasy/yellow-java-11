package com.libertymutual.customer.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.libertymutual.customer.dto.AddressChangeResponse;
import com.libertymutual.customer.dto.AddressRequest;
import com.libertymutual.customer.model.Customer;
import com.libertymutual.customer.service.CustomerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CustomerController.class)
class CustomerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CustomerService customerService;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);

    @Test
    void listCustomers_returnsListResponse() throws Exception {
        Customer c1 = createCustomer("John", "Doe", "MA");
        Customer c2 = createCustomer("Jane", "Smith", "FL");
        when(customerService.listCustomers(null, null, null))
                .thenReturn(Arrays.asList(c1, c2));

        mockMvc.perform(get("/api/v1/customers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(2))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void listCustomers_withStatusFilter_returnsFiltered() throws Exception {
        Customer c1 = createCustomer("John", "Doe", "MA");
        when(customerService.listCustomers("active", null, null))
                .thenReturn(Collections.singletonList(c1));

        mockMvc.perform(get("/api/v1/customers").param("status", "active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(1));
    }

    @Test
    void getCustomerById_found_returnsCustomer() throws Exception {
        UUID id = UUID.randomUUID();
        Customer customer = createCustomer("John", "Doe", "MA");
        customer.setId(id);
        when(customerService.getCustomerById(id)).thenReturn(Optional.of(customer));

        mockMvc.perform(get("/api/v1/customers/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.first_name").value("John"));
    }

    @Test
    void getCustomerById_notFound_returns404() throws Exception {
        UUID id = UUID.randomUUID();
        when(customerService.getCustomerById(id)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/customers/" + id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("not_found"));
    }

    @Test
    void createCustomer_validRequest_returns201() throws Exception {
        Customer saved = createCustomer("John", "Doe", "MA");
        when(customerService.createCustomer(any())).thenReturn(saved);

        String body = "{"
                + "\"first_name\":\"John\","
                + "\"last_name\":\"Doe\","
                + "\"email\":\"john@example.com\","
                + "\"phone\":\"555-0100\","
                + "\"address\":{"
                + "\"street\":\"123 Main St\","
                + "\"city\":\"Boston\","
                + "\"state\":\"MA\","
                + "\"zip\":\"02101\""
                + "}}";

        mockMvc.perform(post("/api/v1/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.first_name").value("John"));
    }

    @Test
    void changeAddress_validRequest_returnsResponse() throws Exception {
        UUID id = UUID.randomUUID();
        Customer updated = createCustomer("John", "Doe", "FL");
        updated.setId(id);

        AddressChangeResponse response = new AddressChangeResponse();
        response.setCustomer(updated);
        response.setPremiumRecalculation(Collections.emptyList());
        AddressChangeResponse.CoverageReevaluation cov = new AddressChangeResponse.CoverageReevaluation();
        cov.setTriggered(true);
        cov.setState("FL");
        cov.setResult("mandatory_review_required");
        response.setCoverageReevaluation(cov);

        when(customerService.changeAddress(eq(id), any(AddressRequest.class))).thenReturn(response);

        String body = "{"
                + "\"street\":\"789 Beach Blvd\","
                + "\"city\":\"Miami\","
                + "\"state\":\"FL\","
                + "\"zip\":\"33101\""
                + "}";

        mockMvc.perform(patch("/api/v1/customers/" + id + "/address")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.coverage_reevaluation.triggered").value(true));
    }

    @Test
    void changeAddress_customerNotFound_returns404() throws Exception {
        UUID id = UUID.randomUUID();
        when(customerService.changeAddress(eq(id), any(AddressRequest.class)))
                .thenThrow(new CustomerService.CustomerNotFoundException("Customer not found"));

        String body = "{"
                + "\"street\":\"789 Beach Blvd\","
                + "\"city\":\"Miami\","
                + "\"state\":\"FL\","
                + "\"zip\":\"33101\""
                + "}";

        mockMvc.perform(patch("/api/v1/customers/" + id + "/address")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound());
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
