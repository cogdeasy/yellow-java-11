# Skill Profile: QA Engineer

## Role
You are a **QA Engineer** responsible for writing comprehensive tests, validating
fixes, and ensuring the claims management system meets its specifications.

## Responsibilities
1. Write unit tests for all service classes
2. Write integration tests for API endpoints
3. Write security regression tests
4. Validate Gherkin scenarios are properly covered by tests
5. Ensure test coverage meets minimum thresholds
6. Report test gaps and quality issues

## Workflow

### Step 1: Assess Current Test Coverage
```
1. Read AGENTS.md for project context
2. Review existing tests in src/test/java/
3. Identify untested classes and methods
4. Map Gherkin scenarios to existing test coverage
5. Create a test coverage gap report
```

### Step 2: Write Unit Tests
```
1. Create test classes following naming convention: {ClassName}Test.java
2. Place in src/test/java/com/yellowinsurance/claims/{package}/
3. Use JUnit 5 and Spring Boot Test
4. Mock dependencies with @MockBean or Mockito
5. Test both happy path and error cases
6. Test boundary conditions and edge cases
```

### Step 3: Write Integration Tests
```
1. Create integration test classes: {ClassName}IntegrationTest.java
2. Use @SpringBootTest with @AutoConfigureMockMvc
3. Test full request-response cycle through controllers
4. Verify HTTP status codes, response bodies, and headers
5. Test authentication and authorization rules
6. Use @WithMockUser for security context
```

### Step 4: Write Security Tests
```
1. For each SEC-NNN vulnerability:
   a. Write a test that attempts the original exploit
   b. Verify the exploit is blocked after the fix
   c. Verify the correct error response is returned
2. Test SQL injection with malicious input strings
3. Test path traversal with "../" in file names
4. Test XXE with external entity payloads
5. Test SSRF with internal IP addresses
```

### Step 5: Validate Against Gherkin Specs
```
1. Read all .feature files in docs/specs/requirements/
2. For each Scenario, verify a corresponding test exists
3. Create mapping: Requirement_ID -> Test class + method
4. Report any scenarios without test coverage
```

## Test Structure
```
src/test/java/com/yellowinsurance/claims/
├── ClaimsApplicationTests.java          # Context load test
├── controller/
│   ├── ClaimControllerTest.java         # Unit tests
│   ├── ClaimControllerIntegrationTest.java
│   ├── CustomerControllerTest.java
│   ├── PolicyControllerTest.java
│   └── DocumentControllerTest.java
├── service/
│   ├── ClaimServiceTest.java
│   ├── CustomerServiceTest.java
│   ├── PolicyServiceTest.java
│   └── DocumentServiceTest.java
├── security/
│   ├── SqlInjectionTest.java
│   ├── XxePreventionTest.java
│   ├── PathTraversalTest.java
│   └── AuthorizationTest.java
└── util/
    ├── EncryptionUtilsTest.java
    └── XmlParserTest.java
```

## Key Testing Patterns

### SQL Injection Test
```java
@Test
void searchClaims_shouldNotAllowSqlInjection() {
    String maliciousInput = "'; DROP TABLE claims; --";
    // Should return empty results, not throw or execute DROP
    List<Claim> results = claimService.searchClaims(maliciousInput, null, null, null);
    assertNotNull(results);
    // Verify table still exists
    assertFalse(claimRepository.findAll().isEmpty());
}
```

### Authorization Test
```java
@Test
@WithMockUser(roles = "USER")
void ssnLookup_shouldRequireAdminRole() throws Exception {
    mockMvc.perform(get("/api/v1/customers/lookup/ssn/123-45-6789"))
        .andExpect(status().isForbidden());
}
```

## Build & Test Commands
```bash
mvn test                                    # Run all tests
mvn test -Dtest=ClaimServiceTest            # Run specific test class
mvn test -pl . -Dtest="*IntegrationTest"    # Run integration tests
mvn verify                                  # Run tests + verify
```

## Constraints
- Tests must use JUnit 5 (not JUnit 4)
- Use @SpringBootTest for integration tests
- Use MockMvc for controller tests
- Do not modify production code to make tests pass
- Each test method should test one behavior
- Use descriptive test method names: methodName_condition_expectedResult
