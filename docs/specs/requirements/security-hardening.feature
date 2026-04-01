@phase-1-approved @security
Feature: Security Hardening
  As a security engineer
  I want to remediate all known vulnerabilities
  So that the application meets OWASP and compliance standards

  Background:
    Given the claims management system is running

  # Requirement_ID: REQ-SEC-001
  @critical @cve
  Scenario: Remediate Log4Shell (CVE-2021-44228)
    Given the application uses Log4j for logging
    When I check the Log4j dependency version
    Then Log4j should be version 2.17.1 or later
    And JNDI lookup should be disabled
    And message lookups should be disabled

  # Requirement_ID: REQ-SEC-002
  @critical @xxe
  Scenario: Prevent XXE in XML parsing
    When I submit XML with external entity declaration:
      """
      <?xml version="1.0"?>
      <!DOCTYPE foo [<!ENTITY xxe SYSTEM "file:///etc/passwd">]>
      <claim><description>&xxe;</description></claim>
      """
    Then the XML parser should reject the document
    And external entity processing should be disabled
    And DTD processing should be disabled

  # Requirement_ID: REQ-SEC-003
  @high @deserialization
  Scenario: Prevent insecure deserialization
    Given the application uses ObjectInputStream
    When I attempt to deserialize a crafted payload
    Then deserialization should use allowlisting
    Or ObjectInputStream should be replaced with safe alternatives (JSON/XML)

  # Requirement_ID: REQ-SEC-004
  @high @crypto
  Scenario: Replace weak cryptographic algorithms
    When I review the encryption utilities
    Then password hashing should use bcrypt with cost factor >= 12
    And encryption should use AES-256-GCM (not DES/ECB)
    And no hardcoded encryption keys should exist in source code
    And encryption keys should be loaded from environment variables or secrets manager

  # Requirement_ID: REQ-SEC-005
  @high @auth
  Scenario: Implement proper authentication and authorization
    Given the application uses Spring Security
    Then CSRF protection should be enabled for state-changing operations
    And passwords should be stored with BCryptPasswordEncoder
    And role-based access control should restrict admin endpoints
    And H2 console should be disabled in production profiles

  # Requirement_ID: REQ-SEC-006
  @medium @headers
  Scenario: Security response headers
    When I make any API request
    Then the response should include X-Content-Type-Options: nosniff
    And the response should include X-Frame-Options: DENY
    And the response should include Strict-Transport-Security header
    And stack traces should NOT be included in error responses

  # Requirement_ID: REQ-SEC-007
  @high @dependencies
  Scenario: Remediate vulnerable dependencies
    When I run dependency vulnerability scanning
    Then all dependencies with CRITICAL or HIGH CVEs should be upgraded
    And commons-collections should be upgraded to 4.x
    And commons-text should be >= 1.10.0
    And Guava should be >= 32.0
    And SnakeYAML should be >= 2.0

  # Requirement_ID: REQ-SEC-008
  @medium @logging
  Scenario: Secure logging practices
    When I review the logging configuration
    Then no PII (SSN, DOB, password) should appear in logs
    And SQL queries should not be logged in production
    And stack traces in logs should be sanitized of sensitive data
    And log files should have restricted file permissions
