# Playbook: Verification & Review (Tech Lead + Security Analyst)

## Phase 5 of the Remediation Workflow

### Purpose
Verify all remediation work is complete, effective, and meets quality standards
before final sign-off.

### Prerequisites
- All implementation streams complete
- All PRs merged
- All tests passing

### Steps

#### Step 1: Security Re-Assessment (Security Analyst)
```
1. Re-scan codebase for each original vulnerability:
   - SEC-001 through SEC-020
2. For each vulnerability:
   a. Verify the fix is in place
   b. Attempt the original exploit
   c. Confirm the exploit is blocked
   d. Check for regression or bypass
3. Scan for new vulnerabilities introduced by fixes
4. Update vulnerability-assessment.md with final status
```

#### Step 2: Test Coverage Review (QA)
```
1. Run tests with coverage: mvn test jacoco:report
2. Review coverage report for each class
3. Ensure minimum thresholds:
   - Service classes: >80% line coverage
   - Controller classes: >70% line coverage
   - Utility classes: >90% line coverage
4. Identify any untested critical paths
5. Write additional tests if needed
```

#### Step 3: ADR Compliance Check (Architect)
```
1. For each accepted ADR:
   a. Verify implementation matches the decision
   b. Check for drift from the ADR
   c. Update ADR status if needed
2. For each PR:
   a. Verify commit messages reference ADR-NNNN
   b. Verify changes align with ADR guidelines
```

#### Step 4: Gherkin Spec Validation (QA + BA)
```
1. For each scenario in docs/specs/requirements/*.feature:
   a. Verify a corresponding test exists
   b. Verify the test passes
   c. Create mapping document: Requirement_ID -> Test
2. Flag any scenarios without coverage
```

#### Step 5: Final Sign-Off
```
1. Build the project: mvn clean package
2. Start the application: java -jar target/*.jar
3. Run smoke tests against all endpoints
4. Verify H2 console is disabled (if production profile)
5. Verify no hardcoded credentials remain
6. Verify no PII in logs
7. Create final summary report
8. Close all GitHub Issues and milestones
```

### Verification Checklist
- [ ] All SEC-NNN vulnerabilities remediated
- [ ] All CQ-NNN code quality issues resolved
- [ ] Test coverage >80% on service classes
- [ ] All Gherkin scenarios have test coverage
- [ ] All ADRs are in ACCEPTED status
- [ ] No hardcoded credentials in codebase
- [ ] No PII logged in application logs
- [ ] Build passes: mvn clean package
- [ ] All GitHub Issues closed
- [ ] Vulnerability assessment updated with final status

### Output Artifacts
- Final `docs/security/vulnerability-assessment.md` with remediation status
- Test coverage report
- Requirement-to-test mapping document
- Summary report for stakeholders
