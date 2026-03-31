# Requirement_ID: REQ-ADDR-001
# Cross-service feature: customer-service -> policy-service -> audit-service
# HITL Gate 1: Pending PO Approval

@customer-service @policy-service @audit-service @phase-1-approved
Feature: Change Home Address with Policy Impact
  As a policyholder
  I want to update my home address
  So that my policy premium and coverage are automatically adjusted for my new location

  Background:
    Given the customer-service is running on port 8081
    And the policy-service is running on port 8082
    And the audit-service is running on port 8083
    And a policyholder exists with an active home insurance policy

  @address-change @happy-path
  Scenario: Change policyholder home address
    Given a policyholder exists with address in "Boston, MA 02101"
    When I send a PATCH request to "/api/v1/customers/{id}/address" with new address "Miami, FL 33101"
    Then the response status should be 200
    And the customer address should be updated to "Miami, FL 33101"
    And an audit event "customer.address_changed" should be emitted
    And the audit event changes should record the old and new address fields

  @premium-calc @happy-path
  Scenario: Automatic premium recalculation on address change
    Given a policyholder has a home insurance policy with premium $1200/year
    And the policyholder changes their address from ZIP 02101 to ZIP 33101
    When the address change is processed
    Then the policy-service should recalculate the premium using ZipRisk API
    And the new premium should reflect the risk factors for ZIP 33101
    And an audit event "policy.premium_recalculated" should be emitted
    And the audit event should contain before and after premium amounts

  @coverage-check @compliance
  Scenario: Mandatory coverage re-evaluation for FL, CA, TX
    Given a policyholder changes their address to a state requiring coverage re-evaluation
    When the address change is processed for state "FL"
    Then coverage eligibility should be re-evaluated
    And flood zone coverage should be assessed for the new ZIP code
    And an audit event "policy.coverage_reevaluated" should be emitted

  @zip-risk @happy-path
  Scenario: ZIP code risk factor lookup
    When I send a GET request to "/api/v1/zip-risk/33101"
    Then the response status should be 200
    And the response should contain risk factors including:
      | factor        | type    |
      | flood_zone    | boolean |
      | hurricane     | float   |
      | wildfire      | float   |
      | crime_rate    | float   |
      | base_modifier | float   |

  @audit-trail @compliance
  Scenario: End-to-end audit trail for address change
    Given a policyholder changes their address
    And the premium is recalculated
    And coverage is re-evaluated
    When I query the audit trail at "/api/v2/audit-trail?entity_type=customer&entity_id={id}"
    Then the audit trail should contain events in chronological order:
      | event_type                    | source            |
      | customer.address_changed      | customer-service  |
      | policy.premium_recalculated   | policy-service    |
      | policy.coverage_reevaluated   | policy-service    |
    And each event should have before/after snapshots
    And each event should have actor and timestamp metadata

  @error
  Scenario: Reject address change with missing required fields
    When I send a PATCH request to "/api/v1/customers/{id}/address" with empty body
    Then the response status should be 400
    And the response should contain validation error details
    And no audit events should be emitted

  @error
  Scenario: Handle premium recalculation failure gracefully
    Given the ZipRisk API is unavailable
    When an address change triggers premium recalculation
    Then the address change should still succeed
    And an audit event "policy.recalculation_failed" should be emitted
    And the policy should be flagged for manual review
