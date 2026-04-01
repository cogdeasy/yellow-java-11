package com.libertymutual.customer.repository;

import com.libertymutual.customer.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, UUID> {

    List<Customer> findByStatus(String status);

    List<Customer> findByState(String state);

    List<Customer> findByCity(String city);

    List<Customer> findByStatusAndState(String status, String state);

    List<Customer> findByStatusAndCity(String status, String city);

    List<Customer> findByStateAndCity(String state, String city);

    List<Customer> findByStatusAndStateAndCity(String status, String state, String city);
}
