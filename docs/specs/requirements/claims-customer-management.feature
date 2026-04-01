@phase-1-approved @customers
Feature: Customer (Policyholder) Management
  As an insurance agent
  I want to manage customer profiles securely
  So that policyholder data is accurate and protected

  Background:
    Given the claims management system is running
    And the user is authenticated with valid credentials

  # Requirement_ID: REQ-CUST-001
  @happy-path
  Scenario: Create a new customer profile
    When I create a customer with:
      | field       | value              |
      | firstName   | Sarah              |
      | lastName    | Connor             |
      | email       | sarah@email.com    |
      | phone       | 555-0199           |
      | ssn         | 111-22-3333        |
      | dateOfBirth | 1985-05-15         |
      | street      | 100 Main St        |
      | city        | Miami              |
      | state       | FL                 |
      | zip         | 33101              |
      | password    | SecureP@ss1        |
    Then the customer should be created with status "ACTIVE"
    And a risk score should be calculated based on location and age

  # Requirement_ID: REQ-CUST-002
  @security @pii
  Scenario: SSN must be encrypted at rest
    Given a customer is created with SSN "111-22-3333"
    When I query the database directly
    Then the SSN column should contain an encrypted value, not plaintext
    And the SSN should only be decryptable with the application's encryption key

  # Requirement_ID: REQ-CUST-003
  @security @pii
  Scenario: SSN must not appear in application logs
    Given a customer is created with SSN "111-22-3333"
    When I review the application log files
    Then no log entry should contain the full SSN "111-22-3333"
    And SSN references in logs should be masked (e.g., "***-**-3333")

  # Requirement_ID: REQ-CUST-004
  @security @password
  Scenario: Passwords must be hashed with bcrypt
    Given a customer is created with password "SecureP@ss1"
    When I query the password_hash column
    Then the hash should start with "$2a$" or "$2b$" (bcrypt prefix)
    And the hash should NOT be an MD5 hex string

  # Requirement_ID: REQ-CUST-005
  @security @sql-injection
  Scenario: Prevent SQL injection in customer search
    When I search customers with name "' OR 1=1 --"
    Then the search should return safely with no results
    And no unauthorized data should be exposed

  # Requirement_ID: REQ-CUST-006
  @security @idor
  Scenario: SSN lookup requires admin authorization
    Given I am authenticated as "viewer" with role "USER"
    When I try to look up a customer by SSN
    Then I should receive a 403 Forbidden response
    And only users with "ADMIN" role should be able to perform SSN lookups

  # Requirement_ID: REQ-CUST-007
  @security @api
  Scenario: Customer API must not expose SSN or password hash
    When I GET /api/v1/customers/1
    Then the response should NOT include the "ssn" field
    And the response should NOT include the "passwordHash" field
    And sensitive fields should be excluded from all customer API responses

  # Requirement_ID: REQ-CUST-008
  @happy-path @premium-calc
  Scenario: Risk score calculation for high-risk states
    When I create a customer in state "FL"
    Then the risk score should be higher than a customer in state "CO"
    And the risk score should factor in hurricane zone risk for Florida
