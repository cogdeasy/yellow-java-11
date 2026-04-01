package com.yellowinsurance.claims.repository;

import com.yellowinsurance.claims.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Optional<Customer> findByEmail(String email);

    List<Customer> findByState(String state);

    List<Customer> findByStatus(String status);

    // VULNERABILITY: Native query with potential for SQL injection via order parameter
    @Query(value = "SELECT * FROM customers WHERE city = ?1", nativeQuery = true)
    List<Customer> findByCity(String city);

    @Query(value = "SELECT * FROM customers WHERE ssn = ?1", nativeQuery = true)
    Optional<Customer> findBySsn(String ssn);
}
