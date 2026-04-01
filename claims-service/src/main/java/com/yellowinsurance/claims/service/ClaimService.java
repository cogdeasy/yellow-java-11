package com.yellowinsurance.claims.service;

import com.yellowinsurance.claims.model.Claim;
import com.yellowinsurance.claims.model.AuditLog;
import com.yellowinsurance.claims.model.dto.ClaimDTO;
import com.yellowinsurance.claims.repository.ClaimRepository;
import com.yellowinsurance.claims.repository.AuditLogRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ISSUES IN THIS CLASS:
 * - God class: handles claims, audit, search, reporting, notifications all in one
 * - SQL injection vulnerabilities via string concatenation
 * - Logs sensitive data (Log4j + Log4Shell vector)
 * - Thread safety issues with shared mutable state
 * - Magic numbers throughout
 * - Resource leaks
 * - Catch-all exception handling
 * - No input validation
 * - Dead code / commented-out code
 */
@Service
public class ClaimService {

    private static final Logger logger = LogManager.getLogger(ClaimService.class);

    @Autowired
    private ClaimRepository claimRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @PersistenceContext
    private EntityManager entityManager;

    // ISSUE: Thread-safety - mutable shared state without proper synchronization
    private Map<String, Object> claimCache = new HashMap<>();
    private int cacheHits = 0;
    private int cacheMisses = 0;

    // ISSUE: Hardcoded configuration values (magic numbers)
    private static final double HIGH_VALUE_THRESHOLD = 50000.0;
    private static final int MAX_CLAIMS_PER_CUSTOMER = 10;
    private static final double AUTO_APPROVE_LIMIT = 5000.0;
    private static final String DEFAULT_ADJUSTER = "system-auto";

    public List<Claim> getAllClaims() {
        return claimRepository.findAll();
    }

    public Optional<Claim> getClaimById(Long id) {
        // ISSUE: Cache implementation with thread-safety problems
        String cacheKey = "claim_" + id;
        if (claimCache.containsKey(cacheKey)) {
            cacheHits++;
            return Optional.of((Claim) claimCache.get(cacheKey));
        }
        cacheMisses++;
        Optional<Claim> claim = claimRepository.findById(id);
        claim.ifPresent(c -> claimCache.put(cacheKey, c));
        return claim;
    }

    public Claim createClaim(ClaimDTO dto) {
        Claim claim = new Claim();
        claim.setClaimNumber(generateClaimNumber());
        claim.setPolicyId(dto.getPolicyId());
        claim.setCustomerId(dto.getCustomerId());
        claim.setClaimType(dto.getClaimType());
        claim.setDescription(dto.getDescription());
        claim.setAmountClaimed(dto.getAmountClaimed());
        claim.setStatus("OPEN");
        claim.setFiledDate(LocalDateTime.now());
        claim.setCreatedAt(LocalDateTime.now());
        claim.setUpdatedAt(LocalDateTime.now());

        if (dto.getIncidentDate() != null) {
            // ISSUE: No error handling for date parsing
            claim.setIncidentDate(LocalDateTime.parse(dto.getIncidentDate(),
                    DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        }

        // ISSUE: Auto-approve without proper authorization checks
        if (dto.getAmountClaimed() != null && dto.getAmountClaimed().doubleValue() <= AUTO_APPROVE_LIMIT) {
            claim.setStatus("AUTO_APPROVED");
            claim.setAmountApproved(dto.getAmountClaimed());
            claim.setAssignedAdjuster(DEFAULT_ADJUSTER);
            claim.setResolvedDate(LocalDateTime.now());
        }

        // VULNERABILITY: Logging user input directly - Log4Shell attack vector
        logger.info("Creating new claim for customer: " + dto.getCustomerId()
                + " with description: " + dto.getDescription());

        Claim savedClaim = claimRepository.save(claim);

        // Audit log
        logAudit("CLAIM", savedClaim.getId(), "CREATED", null, savedClaim.toString());

        return savedClaim;
    }

    public Claim updateClaimStatus(Long id, String newStatus, String adjusterNotes) {
        // ISSUE: No validation of status transitions
        Optional<Claim> existing = claimRepository.findById(id);
        if (!existing.isPresent()) {
            throw new RuntimeException("Claim not found: " + id);
        }

        Claim claim = existing.get();
        String oldStatus = claim.getStatus();
        claim.setStatus(newStatus);
        claim.setAdjusterNotes(adjusterNotes);
        claim.setUpdatedAt(LocalDateTime.now());

        if ("APPROVED".equals(newStatus) || "DENIED".equals(newStatus)) {
            claim.setResolvedDate(LocalDateTime.now());
        }

        // VULNERABILITY: Log4Shell - logging user-controlled input
        logger.info("Claim " + id + " status changed from " + oldStatus + " to " + newStatus
                + " with notes: " + adjusterNotes);

        Claim saved = claimRepository.save(claim);
        logAudit("CLAIM", id, "STATUS_CHANGED", oldStatus, newStatus);

        // Invalidate cache
        claimCache.remove("claim_" + id);

        return saved;
    }

    /**
     * VULNERABILITY: SQL Injection via string concatenation in native query
     * This method builds a raw SQL query from user input without parameterization
     */
    @SuppressWarnings("unchecked")
    public List<Claim> searchClaims(String searchTerm, String status, String sortBy, String sortOrder) {
        // VULNERABILITY: SQL Injection - building query from user input
        StringBuilder sql = new StringBuilder("SELECT * FROM claims WHERE 1=1");

        if (searchTerm != null && !searchTerm.isEmpty()) {
            // VULNERABILITY: Direct string concatenation - SQL injection
            sql.append(" AND (description LIKE '%" + searchTerm + "%'"
                    + " OR claim_number LIKE '%" + searchTerm + "%'"
                    + " OR adjuster_notes LIKE '%" + searchTerm + "%')");
        }

        if (status != null && !status.isEmpty()) {
            // VULNERABILITY: Direct string concatenation - SQL injection
            sql.append(" AND status = '" + status + "'");
        }

        if (sortBy != null && !sortBy.isEmpty()) {
            // VULNERABILITY: Direct string concatenation in ORDER BY - SQL injection
            sql.append(" ORDER BY " + sortBy);
            if (sortOrder != null && !sortOrder.isEmpty()) {
                sql.append(" " + sortOrder);
            }
        } else {
            sql.append(" ORDER BY filed_date DESC");
        }

        logger.debug("Executing search query: " + sql.toString());

        Query query = entityManager.createNativeQuery(sql.toString(), Claim.class);
        return query.getResultList();
    }

    /**
     * VULNERABILITY: SQL Injection in report generation
     */
    @SuppressWarnings("unchecked")
    public List<Object[]> generateReport(String reportType, String startDate, String endDate,
                                          String groupBy) {
        // VULNERABILITY: SQL Injection via string concatenation
        String sql = "SELECT " + groupBy + ", COUNT(*), SUM(amount_claimed), SUM(amount_approved) "
                + "FROM claims WHERE filed_date BETWEEN '" + startDate + "' AND '" + endDate + "' "
                + "GROUP BY " + groupBy;

        logger.info("Generating report: " + reportType + " SQL: " + sql);

        Query query = entityManager.createNativeQuery(sql);
        return query.getResultList();
    }

    public Map<String, Object> getClaimStatistics() {
        Map<String, Object> stats = new HashMap<>();
        List<Claim> allClaims = claimRepository.findAll();

        // ISSUE: Loading all claims into memory for statistics - should use aggregate queries
        int totalClaims = allClaims.size();
        int openClaims = 0;
        int approvedClaims = 0;
        int deniedClaims = 0;
        double totalAmountClaimed = 0;
        double totalAmountApproved = 0;

        for (Claim claim : allClaims) {
            if ("OPEN".equals(claim.getStatus())) openClaims++;
            if ("APPROVED".equals(claim.getStatus())) approvedClaims++;
            if ("DENIED".equals(claim.getStatus())) deniedClaims++;
            if (claim.getAmountClaimed() != null) totalAmountClaimed += claim.getAmountClaimed().doubleValue();
            if (claim.getAmountApproved() != null) totalAmountApproved += claim.getAmountApproved().doubleValue();
        }

        stats.put("totalClaims", totalClaims);
        stats.put("openClaims", openClaims);
        stats.put("approvedClaims", approvedClaims);
        stats.put("deniedClaims", deniedClaims);
        stats.put("totalAmountClaimed", totalAmountClaimed);
        stats.put("totalAmountApproved", totalAmountApproved);
        // ISSUE: Division by zero not handled
        stats.put("approvalRate", (double) approvedClaims / totalClaims * 100);
        stats.put("cacheHitRate", (double) cacheHits / (cacheHits + cacheMisses) * 100);

        return stats;
    }

    // ISSUE: Dead code - method never used
    public void processClaimBatch(List<Long> claimIds) {
        for (Long id : claimIds) {
            try {
                Claim claim = claimRepository.findById(id).orElse(null);
                if (claim != null && "OPEN".equals(claim.getStatus())) {
                    // TODO: implement batch processing logic
                    // claim.setStatus("PROCESSING");
                    // claimRepository.save(claim);
                }
            } catch (Exception e) {
                // ISSUE: Swallowing exceptions silently
                e.printStackTrace();
            }
        }
    }

    // Commented-out code block (code smell)
    // public void archiveOldClaims() {
    //     List<Claim> oldClaims = claimRepository.findAll();
    //     for (Claim claim : oldClaims) {
    //         if (claim.getResolvedDate() != null &&
    //             claim.getResolvedDate().isBefore(LocalDateTime.now().minusYears(7))) {
    //             claim.setStatus("ARCHIVED");
    //             claimRepository.save(claim);
    //         }
    //     }
    // }

    private String generateClaimNumber() {
        // ISSUE: Using insecure random for claim number generation
        Random random = new Random();
        return "CLM-" + System.currentTimeMillis() + "-" + random.nextInt(9999);
    }

    private void logAudit(String entityType, Long entityId, String action,
                          String oldValue, String newValue) {
        try {
            AuditLog log = new AuditLog();
            log.setEntityType(entityType);
            log.setEntityId(entityId);
            log.setAction(action);
            log.setOldValue(oldValue);
            log.setNewValue(newValue);
            log.setPerformedBy("system"); // ISSUE: Not tracking actual user
            log.setCreatedAt(LocalDateTime.now());
            auditLogRepository.save(log);
        } catch (Exception e) {
            // ISSUE: Audit failure silently swallowed - compliance violation
            logger.error("Failed to create audit log", e);
        }
    }

    // ISSUE: Exposing cache internals
    public Map<String, Object> getCacheStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("cacheSize", claimCache.size());
        stats.put("cacheHits", cacheHits);
        stats.put("cacheMisses", cacheMisses);
        stats.put("cacheEntries", claimCache.keySet());
        return stats;
    }

    public void clearCache() {
        claimCache.clear();
        cacheHits = 0;
        cacheMisses = 0;
    }
}
