# Playbook: Test-Forward Development (Developer + QA)

## Phase 3 of the Remediation Workflow

### Purpose
Implement security fixes and code quality improvements following test-driven
development methodology. Tests are written FIRST, then implementation makes
them pass.

### Prerequisites
- Security assessment complete (Playbook 01)
- ADRs approved (Playbook 02)
- Gherkin specs approved (HITL Gate 1)

### Steps

#### Step 1: Set Up Test Infrastructure
```
1. Verify test dependencies in pom.xml:
   - spring-boot-starter-test
   - spring-security-test
2. Create test directory structure:
   src/test/java/com/yellowinsurance/claims/
   ├── controller/
   ├── service/
   ├── security/
   └── util/
3. Run mvn test to verify baseline
```

#### Step 2: Write Security Tests (QA)
```
For each SEC-NNN vulnerability:
1. Create test class in appropriate package
2. Write test that demonstrates the vulnerability:
   @Test
   void searchClaims_withSqlInjection_shouldReturnSafeResults()
3. Write test that verifies the fix works:
   @Test
   void searchClaims_withValidInput_shouldReturnMatchingClaims()
4. Run tests - they should FAIL before the fix
```

#### Step 3: Implement Fixes (Developer)
```
For each failing test:
1. Read the corresponding ADR for guidance
2. Make the minimal change to pass the test
3. Follow the fix patterns in the developer SKILL.md
4. Run tests after each change: mvn test
5. Commit with message format: fix(SEC-NNN): description
```

#### Step 4: Write Code Quality Tests (QA)
```
For each CQ-NNN issue:
1. Write tests that verify correct behavior
2. Write tests for edge cases and error handling
3. Aim for >80% coverage on modified classes
```

#### Step 5: Refactor (Developer)
```
1. Extract god class responsibilities
2. Add validation annotations to DTOs
3. Replace magic numbers with named constants
4. Fix resource leaks with try-with-resources
5. Remove dead code and commented-out code
6. All refactoring must maintain passing tests
```

#### Step 6: Verify
```
1. Run full test suite: mvn test
2. Run application: mvn spring-boot:run
3. Test API endpoints manually
4. Verify no regressions
5. Push and create PR
```

### Constraints
- Tests FIRST, then implementation
- Each commit should reference SEC-NNN or CQ-NNN
- No test modifications to make them pass
- Maintain Java 11 compatibility
