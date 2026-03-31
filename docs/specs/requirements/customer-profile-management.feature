# Requirement_ID: REQ-CUST-001
# HITL Gate 1: Pending PO Approval

@customer-service @phase-1-approved
Feature: Customer Profile Management API
  As a policyholder records administrator
  I want to manage policyholder profiles and addresses
  So that I can maintain accurate customer data across source systems

  Background:
    Given the customer-service is running on port 8081

  @happy-path
  Scenario: Create a new policyholder profile
    Given I have valid customer details including name, email, phone, and address
    When I send a POST request to "/api/v1/customers" with customer data
    Then the response status should be 201
    And the customer status should be "active"
    And an audit event "customer.created" should be emitted

  @happy-path
  Scenario: List policyholders with filtering
    When I send a GET request to "/api/v1/customers?status=active"
    Then the response status should be 200
    And the response should contain a "data" array
    And all returned customers should have status "active"

  @happy-path
  Scenario: Get a single policyholder by ID
    Given a customer exists with a known ID
    When I send a GET request to "/api/v1/customers/{id}"
    Then the response status should be 200
    And the response should contain the customer details including address

  @error
  Scenario: Reject customer creation without required fields
    When I send a POST request to "/api/v1/customers" with empty body
    Then the response status should be 400
    And the response should contain validation error details

  @error
  Scenario: Return 404 for non-existent customer
    When I send a GET request to "/api/v1/customers/{non-existent-id}"
    Then the response status should be 404
