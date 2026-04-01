package com.yellowinsurance.claims.service;

import com.yellowinsurance.claims.model.Customer;
import com.yellowinsurance.claims.model.dto.CustomerDTO;
import com.yellowinsurance.claims.repository.CustomerRepository;
import com.yellowinsurance.claims.util.EncryptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
public class CustomerService {

    private static final Logger logger = LogManager.getLogger(CustomerService.class);

    @Autowired
    private CustomerRepository customerRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    public Optional<Customer> getCustomerById(Long id) {
        return customerRepository.findById(id);
    }

    public Customer createCustomer(CustomerDTO dto) {
        Customer customer = new Customer();
        customer.setFirstName(dto.getFirstName());
        customer.setLastName(dto.getLastName());
        customer.setEmail(dto.getEmail());
        customer.setPhone(dto.getPhone());

        // VULNERABILITY: Storing SSN as plaintext
        customer.setSsn(dto.getSsn());

        if (dto.getDateOfBirth() != null) {
            customer.setDateOfBirth(LocalDate.parse(dto.getDateOfBirth()));
        }

        customer.setStreet(dto.getStreet());
        customer.setCity(dto.getCity());
        customer.setState(dto.getState());
        customer.setZip(dto.getZip());

        // VULNERABILITY: Using MD5 for password hashing
        if (dto.getPassword() != null) {
            customer.setPasswordHash(EncryptionUtils.hashMD5(dto.getPassword()));
        }

        customer.setStatus("ACTIVE");
        customer.setRiskScore(calculateRiskScore(customer));
        customer.setCreatedAt(LocalDateTime.now());
        customer.setUpdatedAt(LocalDateTime.now());

        // VULNERABILITY: Logging PII data including SSN
        logger.info("Creating customer: " + customer.getFirstName() + " " + customer.getLastName()
                + " SSN: " + customer.getSsn() + " Email: " + customer.getEmail());

        return customerRepository.save(customer);
    }

    public Customer updateCustomer(Long id, CustomerDTO dto) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer not found: " + id));

        // ISSUE: Mass assignment - updating all fields without checking which ones changed
        if (dto.getFirstName() != null) customer.setFirstName(dto.getFirstName());
        if (dto.getLastName() != null) customer.setLastName(dto.getLastName());
        if (dto.getEmail() != null) customer.setEmail(dto.getEmail());
        if (dto.getPhone() != null) customer.setPhone(dto.getPhone());
        if (dto.getSsn() != null) customer.setSsn(dto.getSsn());
        if (dto.getStreet() != null) customer.setStreet(dto.getStreet());
        if (dto.getCity() != null) customer.setCity(dto.getCity());
        if (dto.getState() != null) customer.setState(dto.getState());
        if (dto.getZip() != null) customer.setZip(dto.getZip());

        if (dto.getPassword() != null) {
            customer.setPasswordHash(EncryptionUtils.hashMD5(dto.getPassword()));
        }

        customer.setRiskScore(calculateRiskScore(customer));
        customer.setUpdatedAt(LocalDateTime.now());

        return customerRepository.save(customer);
    }

    /**
     * VULNERABILITY: SQL Injection through search
     */
    @SuppressWarnings("unchecked")
    public List<Customer> searchCustomers(String name, String email, String state) {
        // VULNERABILITY: SQL Injection via string concatenation
        StringBuilder sql = new StringBuilder("SELECT * FROM customers WHERE 1=1");

        if (name != null && !name.isEmpty()) {
            sql.append(" AND (first_name LIKE '%" + name + "%' OR last_name LIKE '%" + name + "%')");
        }
        if (email != null && !email.isEmpty()) {
            sql.append(" AND email = '" + email + "'");
        }
        if (state != null && !state.isEmpty()) {
            sql.append(" AND state = '" + state + "'");
        }

        Query query = entityManager.createNativeQuery(sql.toString(), Customer.class);
        return query.getResultList();
    }

    /**
     * VULNERABILITY: IDOR - no authorization check, any user can look up any SSN
     */
    public Optional<Customer> findBySsn(String ssn) {
        // VULNERABILITY: Logging SSN in search
        logger.info("Looking up customer by SSN: " + ssn);
        return customerRepository.findBySsn(ssn);
    }

    // ISSUE: Risk score calculation with magic numbers and no documentation
    private int calculateRiskScore(Customer customer) {
        int score = 50; // base score

        if (customer.getState() != null) {
            switch (customer.getState().toUpperCase()) {
                case "FL": score += 15; break; // hurricane zone
                case "CA": score += 12; break; // wildfire/earthquake
                case "TX": score += 10; break; // tornado/flood
                case "LA": score += 13; break; // hurricane/flood
                case "NY": score += 5; break;
                case "CO": score += 3; break;
                default: break;
            }
        }

        if (customer.getDateOfBirth() != null) {
            int age = LocalDate.now().getYear() - customer.getDateOfBirth().getYear();
            if (age < 25) score += 10;
            if (age > 70) score += 8;
        }

        // ISSUE: Hardcoded max/min without constants
        if (score > 100) score = 100;
        if (score < 0) score = 0;

        return score;
    }
}
