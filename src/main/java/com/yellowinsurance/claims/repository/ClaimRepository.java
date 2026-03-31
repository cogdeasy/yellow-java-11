package com.yellowinsurance.claims.repository;

import com.yellowinsurance.claims.model.Claim;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClaimRepository extends JpaRepository<Claim, Long> {

    List<Claim> findByCustomerId(Long customerId);

    List<Claim> findByPolicyId(Long policyId);

    List<Claim> findByStatus(String status);

    Claim findByClaimNumber(String claimNumber);

    // ISSUE: Using native query when JPQL would be safer and more portable
    @Query(value = "SELECT * FROM claims WHERE assigned_adjuster = ?1 ORDER BY filed_date DESC", nativeQuery = true)
    List<Claim> findByAdjuster(String adjuster);

    @Query(value = "SELECT * FROM claims WHERE status = 'OPEN' AND amount_claimed > ?1", nativeQuery = true)
    List<Claim> findHighValueOpenClaims(double threshold);
}
