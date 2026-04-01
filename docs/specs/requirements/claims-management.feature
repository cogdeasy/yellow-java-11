@phase-1-approved @claims
Feature: Insurance Claims Management
  As a claims adjuster
  I want to manage insurance claims through their lifecycle
  So that policyholders receive timely and accurate claim resolutions

  Background:
    Given the claims management system is running
    And the database contains seed data with customers, policies, and claims
    And the user is authenticated with valid credentials

  # Requirement_ID: REQ-CLM-001
  @happy-path
  Scenario: Create a new insurance claim
    Given a customer with id 1 has an active policy with id 1
    When I submit a new claim with:
      | field         | value                                    |
      | policyId      | 1                                        |
      | customerId    | 1                                        |
      | claimType     | WATER_DAMAGE                             |
      | description   | Pipe burst causing basement flooding     |
      | amountClaimed | 15000.00                                 |
      | incidentDate  | 2024-06-15T10:30:00                      |
    Then the claim should be created with status "OPEN"
    And a unique claim number should be generated
    And an audit log entry should be created for the claim creation

  # Requirement_ID: REQ-CLM-002
  @happy-path @compliance
  Scenario: Auto-approve low-value claims
    Given a customer with an active policy
    When I submit a claim with amount_claimed of $3500.00
    Then the claim should be auto-approved
    And the status should be "AUTO_APPROVED"
    And the assigned_adjuster should be "system-auto"
    And an audit log entry should record the auto-approval

  # Requirement_ID: REQ-CLM-003
  @happy-path
  Scenario: Update claim status through adjuster review
    Given an existing claim with id 1 and status "OPEN"
    When the adjuster updates the status to "APPROVED" with notes "Verified damage"
    Then the claim status should be "APPROVED"
    And the resolved_date should be set
    And an audit log should record the status change from "OPEN" to "APPROVED"

  # Requirement_ID: REQ-CLM-004
  @error
  Scenario: Reject claim status update for non-existent claim
    When I try to update the status of claim id 99999
    Then I should receive a 404 Not Found response
    And the error message should indicate the claim was not found

  # Requirement_ID: REQ-CLM-005
  @security @sql-injection
  Scenario: Prevent SQL injection in claim search
    When I search for claims with search term "'; DROP TABLE claims; --"
    Then the search should return safely with no results
    And the claims table should still exist with all data intact

  # Requirement_ID: REQ-CLM-006
  @security @sql-injection
  Scenario: Prevent SQL injection in report generation
    When I generate a report with groupBy parameter "status; DROP TABLE claims"
    Then I should receive a 400 Bad Request response
    And the request should be rejected before executing any SQL

  # Requirement_ID: REQ-CLM-007
  @compliance @audit
  Scenario: All claim mutations produce audit trail entries
    Given an existing claim with id 1
    When the claim is created, updated, or status-changed
    Then each mutation should produce an audit log entry
    And the audit log should include entity_type, entity_id, action, and timestamp
    And the audit log should record the performing user (not "system")
