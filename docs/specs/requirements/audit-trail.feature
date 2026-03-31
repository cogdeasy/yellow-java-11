# Requirement_ID: REQ-COMP-001
# HITL Gate 1: Pending PO Approval

@audit-service @phase-1-approved
Feature: Compliance Audit Trail
  As a compliance officer
  I want to query an aggregated audit trail across all source systems
  So that I can verify regulatory compliance and trace all changes

  Background:
    Given the audit-service is running on port 8083
    And audit events have been ingested from customer-service and policy-service

  @happy-path
  Scenario: Query aggregated audit trail
    When I send a GET request to "/api/v2/audit-trail"
    Then the response status should be 200
    And the response should contain a "data" array of audit events
    And each event should have specversion "1.0" (CloudEvents format)

  @happy-path
  Scenario: Filter audit trail by entity type
    When I send a GET request to "/api/v2/audit-trail?entity_type=customer"
    Then the response status should be 200
    And all returned events should have entity_type "customer"

  @happy-path
  Scenario: Get audit trail for a specific entity
    Given a customer with known ID has audit events
    When I send a GET request to "/api/v2/audit-trail/customer/{id}"
    Then the response status should be 200
    And the response should contain all events for that customer in chronological order

  @happy-path
  Scenario: Audit trail summary statistics
    When I send a GET request to "/api/v2/audit-trail/summary/stats"
    Then the response status should be 200
    And the response should contain:
      | field            | type   |
      | total_events     | number |
      | events_by_type   | object |
      | events_by_source | object |
      | time_range       | object |

  @compliance
  Scenario: Ingest audit event in CloudEvents format
    When I send a POST request to "/api/v2/audit-events" with a valid CloudEvents payload
    Then the response status should be 201
    And the event should be stored immutably
    And the event should be queryable via the audit trail API

  @compliance
  Scenario: Reject malformed audit events
    When I send a POST request to "/api/v2/audit-events" with invalid CloudEvents payload
    Then the response status should be 400
    And the error should indicate which CloudEvents fields are missing

  @enrichment
  Scenario: Cross-service enrichment of audit events
    Given an audit event references a customer_id
    When I query the audit trail for that event
    Then the event should be enriched with customer name and current address
    And the event should be enriched with related policy information
