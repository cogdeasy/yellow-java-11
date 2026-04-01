package com.libertymutual.customer;

import com.libertymutual.customer.model.Customer;
import com.libertymutual.customer.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class CustomerServiceIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("liberty_mutual_test")
            .withUsername("test")
            .withPassword("test")
            .withInitScript("schema.sql");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "none");
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CustomerRepository customerRepository;

    @BeforeEach
    void setUp() {
        customerRepository.deleteAll();
    }

    @Test
    void createAndRetrieveCustomer() throws Exception {
        String body = "{"
                + "\"first_name\":\"John\","
                + "\"last_name\":\"Doe\","
                + "\"email\":\"john.doe@example.com\","
                + "\"phone\":\"555-0100\","
                + "\"address\":{"
                + "\"street\":\"123 Main St\","
                + "\"city\":\"Boston\","
                + "\"state\":\"MA\","
                + "\"zip\":\"02101\""
                + "}}";

        String response = mockMvc.perform(post("/api/v1/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.first_name").value("John"))
                .andExpect(jsonPath("$.last_name").value("Doe"))
                .andExpect(jsonPath("$.state").value("MA"))
                .andReturn().getResponse().getContentAsString();

        // Extract ID from response
        String id = com.fasterxml.jackson.databind.ObjectMapper
                .class.getDeclaredConstructor().newInstance()
                .readTree(response).get("id").asText();

        mockMvc.perform(get("/api/v1/customers/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.first_name").value("John"));
    }

    @Test
    void listCustomers_withFilters() throws Exception {
        // Create two customers
        createTestCustomer("Alice", "Smith", "alice@example.com", "MA", "02101");
        createTestCustomer("Bob", "Jones", "bob@example.com", "FL", "33101");

        mockMvc.perform(get("/api/v1/customers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(2));

        mockMvc.perform(get("/api/v1/customers").param("state", "FL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.data[0].first_name").value("Bob"));
    }

    @Test
    void getCustomer_notFound_returns404() throws Exception {
        mockMvc.perform(get("/api/v1/customers/" + UUID.randomUUID()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("not_found"));
    }

    @Test
    void createCustomer_invalidEmail_returns400() throws Exception {
        String body = "{"
                + "\"first_name\":\"John\","
                + "\"last_name\":\"Doe\","
                + "\"email\":\"not-an-email\","
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
                .andExpect(status().isBadRequest());
    }

    @Test
    void healthCheck_returnsHealthy() throws Exception {
        mockMvc.perform(get("/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("healthy"))
                .andExpect(jsonPath("$.service").value("customer-service"));
    }

    private void createTestCustomer(String firstName, String lastName, String email,
                                     String state, String zip) {
        Customer customer = new Customer();
        customer.setId(UUID.randomUUID());
        customer.setFirstName(firstName);
        customer.setLastName(lastName);
        customer.setEmail(email);
        customer.setPhone("555-0100");
        customer.setStreet("123 Main St");
        customer.setCity("Boston");
        customer.setState(state);
        customer.setZip(zip);
        customer.setStatus("active");
        customer.setCreatedAt(OffsetDateTime.now());
        customer.setUpdatedAt(OffsetDateTime.now());
        customerRepository.save(customer);
    }
}
