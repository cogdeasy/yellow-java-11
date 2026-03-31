# Requirement_ID: REQ-POL-001
# HITL Gate 1: Pending PO Approval

@policy-service @phase-1-approved
Feature: Policy Management API
  As a policy platform administrator
  I want to manage insurance policies and premiums
  So that I can maintain accurate policy data and pricing

  Background:
    Given the policy-service is running on port 8082

  @happy-path
  Scenario: Create a new insurance policy
    Given I have valid policy details including type, customer_id, and coverage
    When I send a POST request to "/api/v1/policies" with policy data
    Then the response status should be 201
    And the policy status should be "active"
    And an audit event "policy.created" should be emitted

  @happy-path
  Scenario: List policies with filtering
    When I send a GET request to "/api/v1/policies?status=active"
    Then the response status should be 200
    And the response should contain a "data" array
    And all returned policies should have status "active"

  @happy-path
  Scenario: Get policies for a specific customer
    Given a customer has one or more active policies
    When I send a GET request to "/api/v1/policies?customer_id={id}"
    Then the response status should be 200
    And all returned policies should belong to the specified customer

  @happy-path
  Scenario: Get a single policy by ID
    Given a policy exists with a known ID
    When I send a GET request to "/api/v1/policies/{id}"
    Then the response status should be 200
    And the response should contain the policy details including premium and coverage

  @premium-calc
  Scenario: Recalculate policy premium
    Given a policy exists with a known premium
    When I send a POST request to "/api/v1/policies/{id}/recalculate" with new ZIP code
    Then the response status should be 200
    And the premium should be recalculated based on ZipRisk factors
    And an audit event "policy.premium_recalculated" should be emitted

  @error
  Scenario: Reject policy creation without required fields
    When I send a POST request to "/api/v1/policies" with empty body
    Then the response status should be 400

  @error
  Scenario: Return 404 for non-existent policy
    When I send a GET request to "/api/v1/policies/{non-existent-id}"
    Then the response status should be 404
