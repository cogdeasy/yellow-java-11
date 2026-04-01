package com.yellowinsurance.claims.repository;

import com.yellowinsurance.claims.model.Policy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PolicyRepository extends JpaRepository<Policy, Long> {

    List<Policy> findByCustomerId(Long customerId);

    Policy findByPolicyNumber(String policyNumber);

    List<Policy> findByStatus(String status);

    List<Policy> findByPolicyType(String policyType);

    @Query(value = "SELECT * FROM policies WHERE customer_id = ?1 AND status = 'ACTIVE'", nativeQuery = true)
    List<Policy> findActivePoliciesByCustomer(Long customerId);
}
