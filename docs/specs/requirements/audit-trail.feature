Feature: Audit Trail
  As a Liberty Mutual compliance system
  I need to track all data changes via CloudEvents v1.0
  So that we maintain a complete, immutable audit trail

  Background:
    Given the audit-service is running on port 3003
    And the database contains the standard schema

  Scenario: Ingest a valid CloudEvent
    When I POST to "/api/v2/audit-events" with a CloudEvent:
      | specversion | type             | source            | id                                   |
      | 1.0         | customer.created | /customer-service | 550e8400-e29b-41d4-a716-446655440000 |
    Then the response status should be 201
    And the response "status" should be "ingested"

  Scenario: Reject CloudEvent with wrong specversion
    When I POST to "/api/v2/audit-events" with specversion "0.3"
    Then the response status should be 400
    And the response should contain "Only CloudEvents specversion 1.0 is supported"

  Scenario: Reject CloudEvent with missing required fields
    When I POST to "/api/v2/audit-events" with only specversion
    Then the response status should be 400
    And the response "error" should be "validation_error"

  Scenario: Retrieve aggregated audit trail
    Given audit events exist for customer and policy operations
    When I GET "/api/v2/audit-trail"
    Then the response status should be 200
    And the response should contain enriched events

  Scenario: Retrieve entity-specific audit trail
    Given a customer with ID "a1b2c3d4" has 3 audit events
    When I GET "/api/v2/audit-trail/customer/a1b2c3d4"
    Then the response status should be 200
    And the response "total" should be 3
    And events should be ordered by time ascending

  Scenario: Retrieve audit summary statistics
    Given the audit trail contains 42 events
    When I GET "/api/v2/audit-trail/summary/stats"
    Then the response status should be 200
    And the response "total_events" should be 42
    And the response should contain "events_by_type"
    And the response should contain "events_by_source"
    And the response should contain "time_range"
