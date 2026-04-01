package com.yellowinsurance.claims.model.dto;

import java.math.BigDecimal;

// ISSUE: No validation annotations on DTO fields
// ISSUE: Mutable DTO with no builder pattern
public class ClaimDTO {

    private Long policyId;
    private Long customerId;
    private String claimType;
    private String description;
    private BigDecimal amountClaimed;
    private String incidentDate;
    private String adjusterNotes;

    public ClaimDTO() {}

    public Long getPolicyId() { return policyId; }
    public void setPolicyId(Long policyId) { this.policyId = policyId; }

    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }

    public String getClaimType() { return claimType; }
    public void setClaimType(String claimType) { this.claimType = claimType; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getAmountClaimed() { return amountClaimed; }
    public void setAmountClaimed(BigDecimal amountClaimed) { this.amountClaimed = amountClaimed; }

    public String getIncidentDate() { return incidentDate; }
    public void setIncidentDate(String incidentDate) { this.incidentDate = incidentDate; }

    public String getAdjusterNotes() { return adjusterNotes; }
    public void setAdjusterNotes(String adjusterNotes) { this.adjusterNotes = adjusterNotes; }
}
