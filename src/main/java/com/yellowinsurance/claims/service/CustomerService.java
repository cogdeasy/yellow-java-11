package com.yellowinsurance.claims.service;

import com.yellowinsurance.claims.model.AuditLog;
import com.yellowinsurance.claims.model.Customer;
import com.yellowinsurance.claims.model.Policy;
import com.yellowinsurance.claims.model.dto.CustomerDTO;
import com.yellowinsurance.claims.repository.AuditLogRepository;
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

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private PolicyService policyService;

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

    /**
     * Deactivate a customer account.
     * ISSUE: No check for active policies or open claims before deactivation
     */
    public Customer deactivateCustomer(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer not found: " + id));

        if ("INACTIVE".equals(customer.getStatus())) {
            throw new RuntimeException("Customer is already inactive");
        }

        String oldStatus = customer.getStatus();
        customer.setStatus("INACTIVE");
        customer.setUpdatedAt(LocalDateTime.now());

        // VULNERABILITY: Logging PII on status change
        logger.info("Deactivating customer: " + customer.getFirstName() + " " + customer.getLastName()
                + " SSN: " + customer.getSsn());

        Customer saved = customerRepository.save(customer);
        logAudit("CUSTOMER", id, "DEACTIVATED", oldStatus, "INACTIVE");
        return saved;
    }

    /**
     * Delete a customer.
     * ISSUE: Hard delete, no cascading cleanup of policies/claims
     * ISSUE: No authorization check
     */
    public void deleteCustomer(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer not found: " + id));

        // VULNERABILITY: Logging full PII on deletion
        logger.info("Deleting customer: " + customer.toString());

        customerRepository.delete(customer);
        logAudit("CUSTOMER", id, "DELETED", customer.getStatus(), null);
    }

    /**
     * Process an address change for a customer.
     * ISSUE: No address validation, no notification to underwriting
     * VULNERABILITY: Logging full address including old and new
     */
    public Customer changeAddress(Long id, String street, String city, String state, String zip) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer not found: " + id));

        String oldAddress = customer.getStreet() + ", " + customer.getCity() + ", "
                + customer.getState() + " " + customer.getZip();
        String oldZip = customer.getZip();
        String oldState = customer.getState();

        customer.setStreet(street);
        customer.setCity(city);
        customer.setState(state);
        customer.setZip(zip);
        customer.setRiskScore(calculateRiskScore(customer));
        customer.setUpdatedAt(LocalDateTime.now());

        String newAddress = street + ", " + city + ", " + state + " " + zip;

        // VULNERABILITY: Logging full addresses
        logger.info("Address changed for customer " + id + ": " + oldAddress + " -> " + newAddress);

        Customer saved = customerRepository.save(customer);
        logAudit("CUSTOMER", id, "ADDRESS_CHANGED", oldAddress, newAddress);

        // Trigger mandatory coverage re-evaluation for FL, CA, TX
        if ((state != null && policyService.requiresCoverageReview(state)) || (oldState != null && policyService.requiresCoverageReview(oldState))) {
            List<Policy> activePolicies = policyService.getPoliciesByCustomer(id);
            for (Policy policy : activePolicies) {
                if ("ACTIVE".equals(policy.getStatus())) {
                    try {
                        policyService.recalculatePremium(policy.getId(), zip, oldZip);
                        logger.info("Coverage re-evaluated for policy " + policy.getId()
                                + " due to address change to " + state);
                    } catch (Exception e) {
                        // ISSUE: Silently swallowing recalculation errors
                        logger.error("Failed to recalculate premium for policy " + policy.getId(), e);
                    }
                }
            }
        }

        return saved;
    }

    private void logAudit(String entityType, Long entityId, String action, String oldValue, String newValue) {
        try {
            AuditLog log = new AuditLog();
            log.setEntityType(entityType);
            log.setEntityId(entityId);
            log.setAction(action);
            log.setPerformedBy("system");
            log.setOldValue(oldValue);
            log.setNewValue(newValue);
            log.setCreatedAt(LocalDateTime.now());
            auditLogRepository.save(log);
        } catch (Exception e) {
            logger.error("Failed to create audit log", e);
        }
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
