@phase-1-approved @policies
Feature: Policy Management and Premium Calculation
  As an underwriter
  I want to manage insurance policies and recalculate premiums
  So that premiums accurately reflect location-based risk

  Background:
    Given the claims management system is running
    And the user is authenticated with valid credentials
    And ZIP risk factors are configured for known ZIP codes

  # Requirement_ID: REQ-POL-001
  @happy-path @premium-calc
  Scenario: Recalculate premium after address change
    Given customer 1 has policy "HOME-2024-0001" with premium $2450.00
    And the current ZIP code is "33101" (Miami, FL) with risk factor 1.45
    When the customer moves to ZIP code "80201" (Denver, CO) with risk factor 1.05
    Then the premium should be recalculated using the new risk factor
    And the new premium should be lower than the original
    And the change should be recorded with old and new premium values

  # Requirement_ID: REQ-POL-002
  @happy-path @coverage-check
  Scenario: Mandatory coverage re-evaluation for FL/CA/TX
    Given a customer moves to state "FL"
    When the address change is processed
    Then the system should flag mandatory coverage re-evaluation
    And the review should be triggered for hurricane zone coverage

  # Requirement_ID: REQ-POL-003
  @compliance @audit
  Scenario: Premium changes must have audit trail
    Given an existing policy with a known premium
    When the premium is recalculated
    Then an audit log entry should record the old premium and new premium
    And the audit log should identify who triggered the recalculation
    And the audit log should record the timestamp

  # Requirement_ID: REQ-POL-004
  @code-quality @money
  Scenario: Premium calculations use proper decimal arithmetic
    Given a policy with premium $2450.00
    When the premium is recalculated multiple times
    Then all intermediate and final values should use BigDecimal
    And no floating-point arithmetic should be used for money
    And the result should be rounded to 2 decimal places using HALF_UP

  # Requirement_ID: REQ-POL-005
  @happy-path
  Scenario: Look up ZIP code risk factors
    When I query the risk factor for ZIP code "33101"
    Then I should receive risk factor 1.45
    And the response should indicate it is a known ZIP code

  # Requirement_ID: REQ-POL-006
  @error
  Scenario: Handle unknown ZIP codes gracefully
    When I query the risk factor for ZIP code "99999"
    Then I should receive the default risk factor of 1.0
    And the response should indicate it is NOT a known ZIP code
