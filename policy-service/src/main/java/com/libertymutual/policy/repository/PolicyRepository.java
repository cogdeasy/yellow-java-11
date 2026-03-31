package com.libertymutual.policy.repository;

import com.libertymutual.policy.model.Policy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PolicyRepository extends JpaRepository<Policy, UUID> {

    List<Policy> findByCustomerId(UUID customerId);

    List<Policy> findByType(String type);

    List<Policy> findByStatus(String status);

    List<Policy> findByCustomerIdAndType(UUID customerId, String type);

    List<Policy> findByCustomerIdAndStatus(UUID customerId, String status);

    List<Policy> findByTypeAndStatus(String type, String status);

    List<Policy> findByCustomerIdAndTypeAndStatus(UUID customerId, String type, String status);
}
