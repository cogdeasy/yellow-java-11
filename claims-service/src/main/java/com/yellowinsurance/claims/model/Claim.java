package com.yellowinsurance.claims.model;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "claims")
public class Claim {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "claim_number", unique = true)
    private String claimNumber;

    @Column(name = "policy_id")
    private Long policyId;

    @Column(name = "customer_id")
    private Long customerId;

    @Column(name = "claim_type")
    private String claimType;

    @Column(name = "description", length = 4000)
    private String description;

    @Column(name = "status")
    private String status;

    @Column(name = "amount_claimed")
    private BigDecimal amountClaimed;

    @Column(name = "amount_approved")
    private BigDecimal amountApproved;

    @Column(name = "incident_date")
    private LocalDateTime incidentDate;

    @Column(name = "filed_date")
    private LocalDateTime filedDate;

    @Column(name = "resolved_date")
    private LocalDateTime resolvedDate;

    @Column(name = "adjuster_notes", length = 4000)
    private String adjusterNotes;

    @Column(name = "assigned_adjuster")
    private String assignedAdjuster;

    // ISSUE: Storing raw file paths - potential path traversal
    @Column(name = "document_path")
    private String documentPath;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ISSUE: No @PrePersist/@PreUpdate for automatic timestamp management
    // ISSUE: No validation annotations

    public Claim() {}

    // Getters and Setters (no Lombok to demonstrate boilerplate issue)
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getClaimNumber() { return claimNumber; }
    public void setClaimNumber(String claimNumber) { this.claimNumber = claimNumber; }

    public Long getPolicyId() { return policyId; }
    public void setPolicyId(Long policyId) { this.policyId = policyId; }

    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }

    public String getClaimType() { return claimType; }
    public void setClaimType(String claimType) { this.claimType = claimType; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public BigDecimal getAmountClaimed() { return amountClaimed; }
    public void setAmountClaimed(BigDecimal amountClaimed) { this.amountClaimed = amountClaimed; }

    public BigDecimal getAmountApproved() { return amountApproved; }
    public void setAmountApproved(BigDecimal amountApproved) { this.amountApproved = amountApproved; }

    public LocalDateTime getIncidentDate() { return incidentDate; }
    public void setIncidentDate(LocalDateTime incidentDate) { this.incidentDate = incidentDate; }

    public LocalDateTime getFiledDate() { return filedDate; }
    public void setFiledDate(LocalDateTime filedDate) { this.filedDate = filedDate; }

    public LocalDateTime getResolvedDate() { return resolvedDate; }
    public void setResolvedDate(LocalDateTime resolvedDate) { this.resolvedDate = resolvedDate; }

    public String getAdjusterNotes() { return adjusterNotes; }
    public void setAdjusterNotes(String adjusterNotes) { this.adjusterNotes = adjusterNotes; }

    public String getAssignedAdjuster() { return assignedAdjuster; }
    public void setAssignedAdjuster(String assignedAdjuster) { this.assignedAdjuster = assignedAdjuster; }

    public String getDocumentPath() { return documentPath; }
    public void setDocumentPath(String documentPath) { this.documentPath = documentPath; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    // ISSUE: toString exposes all fields including sensitive data
    @Override
    public String toString() {
        return "Claim{id=" + id + ", claimNumber='" + claimNumber + "', policyId=" + policyId
                + ", customerId=" + customerId + ", type='" + claimType + "', status='" + status
                + "', amountClaimed=" + amountClaimed + ", amountApproved=" + amountApproved
                + ", adjusterNotes='" + adjusterNotes + "', documentPath='" + documentPath + "'}";
    }
}
