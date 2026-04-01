@phase-1-approved @documents
Feature: Document Management
  As a claims adjuster
  I want to securely upload and manage claim documents
  So that evidence and supporting materials are properly stored

  Background:
    Given the claims management system is running
    And the user is authenticated with valid credentials

  # Requirement_ID: REQ-DOC-001
  @security @path-traversal
  Scenario: Prevent path traversal in file upload
    When I upload a file with name "../../etc/passwd"
    Then the upload should be rejected with a validation error
    And the file should NOT be written outside the upload directory

  # Requirement_ID: REQ-DOC-002
  @security @path-traversal
  Scenario: Prevent path traversal in file download
    Given a document record exists in the database
    When the file_path has been tampered with to "/etc/shadow"
    Then the download should be rejected
    And only files within the configured upload directory should be readable

  # Requirement_ID: REQ-DOC-003
  @security @ssrf
  Scenario: Prevent SSRF in document import
    When I try to import a document from URL "http://169.254.169.254/latest/meta-data/"
    Then the import should be rejected
    And internal/private IP ranges should be blocked
    And only HTTPS URLs to public hosts should be allowed

  # Requirement_ID: REQ-DOC-004
  @security @file-validation
  Scenario: Validate uploaded file types
    When I upload a file with extension ".exe"
    Then the upload should be rejected
    And only allowed file types (PDF, JPG, PNG, DOCX) should be accepted

  # Requirement_ID: REQ-DOC-005
  @security @file-validation
  Scenario: Enforce file size limits
    When I upload a file larger than 10MB
    Then the upload should be rejected with a file size error
    And the maximum allowed file size should be configurable

  # Requirement_ID: REQ-DOC-006
  @security @zip-slip
  Scenario: Prevent Zip Slip in archive extraction
    Given a malicious ZIP file with entry name "../../etc/cron.d/malicious"
    When the ZIP is extracted
    Then the extraction should reject entries that escape the target directory
    And no files should be written outside the designated extraction directory
