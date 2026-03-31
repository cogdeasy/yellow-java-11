Feature: Customer Management
  As a Liberty Mutual insurance platform
  I need to manage policyholder records
  So that customer data is accurate and auditable

  Background:
    Given the customer-service is running on port 3001
    And the database contains the standard schema

  Scenario: Create a new customer
    When I POST to "/api/v1/customers" with:
      | first_name | last_name | email              | phone    | street       | city   | state | zip   |
      | John       | Doe       | john@example.com   | 555-0100 | 123 Main St  | Boston | MA    | 02101 |
    Then the response status should be 201
    And the response should contain "id"
    And the response "status" should be "active"
    And an audit event "customer.created" should be emitted

  Scenario: List customers with no filters
    Given the database contains 5 customers
    When I GET "/api/v1/customers"
    Then the response status should be 200
    And the response "total" should be 5

  Scenario: List customers filtered by state
    Given the database contains customers in states "MA", "FL", "CA"
    When I GET "/api/v1/customers?state=FL"
    Then the response status should be 200
    And all returned customers should have state "FL"

  Scenario: Retrieve a customer by ID
    Given a customer exists with ID "a1b2c3d4-e5f6-4a7b-8c9d-0e1f2a3b4c5d"
    When I GET "/api/v1/customers/a1b2c3d4-e5f6-4a7b-8c9d-0e1f2a3b4c5d"
    Then the response status should be 200
    And the response "first_name" should be "John"

  Scenario: Retrieve a non-existent customer
    When I GET "/api/v1/customers/00000000-0000-0000-0000-000000000000"
    Then the response status should be 404
    And the response "error" should be "not_found"

  Scenario: Change customer address (same state)
    Given a customer exists in state "MA" with zip "02101"
    When I PATCH "/api/v1/customers/{id}/address" with:
      | street      | city      | state | zip   |
      | 456 Oak Ave | Cambridge | MA    | 02102 |
    Then the response status should be 200
    And the customer address should be updated
    And an audit event "customer.address_changed" should be emitted
    And "coverage_reevaluation.triggered" should be false

  Scenario: Change customer address to Florida (mandatory review)
    Given a customer exists in state "MA" with zip "02101"
    When I PATCH "/api/v1/customers/{id}/address" with:
      | street         | city  | state | zip   |
      | 789 Beach Blvd | Miami | FL    | 33101 |
    Then the response status should be 200
    And "coverage_reevaluation.triggered" should be true
    And "coverage_reevaluation.state" should be "FL"
