package com.yellowinsurance.claims.service;

import com.yellowinsurance.claims.model.Claim;
import com.yellowinsurance.claims.repository.ClaimRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.*;
import java.util.List;

@Service
public class ReportService {

    private static final Logger logger = LogManager.getLogger(ReportService.class);

    @Autowired
    private ClaimRepository claimRepository;

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * ISSUE: Generates CSV in memory - will OOM on large datasets
     * ISSUE: No streaming, loads everything into StringBuilder
     * VULNERABILITY: CSV injection - user data not sanitized for CSV formula injection
     */
    public String generateClaimsCsv(String status) {
        List<Claim> claims;
        if (status != null && !status.isEmpty()) {
            claims = claimRepository.findByStatus(status);
        } else {
            claims = claimRepository.findAll();
        }

        StringBuilder csv = new StringBuilder();
        csv.append("Claim Number,Customer ID,Policy ID,Type,Status,Amount Claimed,Amount Approved,Description,Filed Date\n");

        for (Claim claim : claims) {
            // VULNERABILITY: CSV injection - description could contain =CMD(), +CMD() etc.
            csv.append(claim.getClaimNumber()).append(",");
            csv.append(claim.getCustomerId()).append(",");
            csv.append(claim.getPolicyId()).append(",");
            csv.append(claim.getClaimType()).append(",");
            csv.append(claim.getStatus()).append(",");
            csv.append(claim.getAmountClaimed()).append(",");
            csv.append(claim.getAmountApproved()).append(",");
            // VULNERABILITY: No escaping of description field - CSV injection
            csv.append(claim.getDescription()).append(",");
            csv.append(claim.getFiledDate()).append("\n");
        }

        return csv.toString();
    }

    /**
     * VULNERABILITY: Path traversal in report export
     * ISSUE: Resource leak - writer not in try-with-resources
     */
    public String exportReport(String reportName, String content) {
        try {
            // VULNERABILITY: Path traversal - reportName not sanitized
            String filePath = "/tmp/reports/" + reportName + ".csv";
            File file = new File(filePath);
            file.getParentFile().mkdirs();

            // ISSUE: Resource leak
            FileWriter writer = new FileWriter(file);
            writer.write(content);
            writer.close();

            logger.info("Report exported to: " + filePath);
            return filePath;
        } catch (IOException e) {
            throw new RuntimeException("Failed to export report", e);
        }
    }
}
