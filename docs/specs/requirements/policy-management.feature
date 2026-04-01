Feature: Policy Management
  As a Liberty Mutual insurance platform
  I need to manage insurance policies and calculate premiums
  So that policies reflect accurate risk-based pricing

  Background:
    Given the policy-service is running on port 3002
    And the database contains the standard schema

  Scenario: Create a policy with known ZIP code
    When I POST to "/api/v1/policies" with:
      | customer_id                          | type | coverage_amount | zip_code |
      | a1b2c3d4-e5f6-4a7b-8c9d-0e1f2a3b4c5d | home | 500000          | 33101    |
    Then the response status should be 201
    And the response "premium_annual" should be 8750.00
    And an audit event "policy.created" should be emitted

  Scenario: Create a policy with unknown ZIP code
    When I POST to "/api/v1/policies" with:
      | customer_id                          | type | coverage_amount | zip_code |
      | a1b2c3d4-e5f6-4a7b-8c9d-0e1f2a3b4c5d | auto | 300000          | 99999    |
    Then the response status should be 201
    And the response "premium_annual" should be 3000.00

  Scenario: Recalculate premium for address change to high-risk area
    Given a policy exists with coverage 500000 and zip "02101"
    When I POST to "/api/v1/policies/{id}/recalculate" with:
      | new_zipcode | old_zipcode |
      | 33101       | 02101       |
    Then the response status should be 200
    And the "new_premium" should be 8750.00
    And an audit event "policy.premium_recalculated" should be emitted

  Scenario: Recalculate premium for unknown ZIP code
    Given a policy exists with coverage 500000 and zip "02101"
    When I POST to "/api/v1/policies/{id}/recalculate" with:
      | new_zipcode | old_zipcode |
      | 99999       | 02101       |
    Then the response status should be 200
    And the "status" should be "pending_review"

  Scenario: Query ZIP risk data
    When I GET "/api/v1/zip-risk/33101"
    Then the response status should be 200
    And the "state" should be "FL"
    And the "flood_zone" should be true
    And the "base_modifier" should be 1.75

  Scenario: Query unknown ZIP risk data
    When I GET "/api/v1/zip-risk/99999"
    Then the response status should be 404

  Scenario: List policies by customer
    Given customer has 2 active policies
    When I GET "/api/v1/policies?customer_id={customer_id}"
    Then the response status should be 200
    And the response "total" should be 2
