package com.yellowinsurance.claims.repository;

import com.yellowinsurance.claims.model.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {

    List<Document> findByClaimId(Long claimId);

    List<Document> findByCustomerId(Long customerId);
}
