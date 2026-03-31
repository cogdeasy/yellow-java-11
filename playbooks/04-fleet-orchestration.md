# Playbook: Fleet Orchestration (Tech Lead)

## Phase 4 of the Remediation Workflow

### Purpose
Coordinate multiple Devin sessions working in parallel on different aspects
of the remediation, ensuring no conflicts and proper integration.

### Prerequisites
- All ADRs approved
- GitHub Issues created for all work items
- Clear work breakdown by role and phase

### Steps

#### Step 1: Create Work Streams
```
Decompose work into independent streams:

Stream A: Critical Security Fixes
  - SEC-004: Log4j upgrade (pom.xml only)
  - SEC-009: Hardcoded credentials removal
  - SEC-020: Dependency upgrades

Stream B: SQL Injection Fixes
  - SEC-001: ClaimService.searchClaims()
  - SEC-002: CustomerService.searchCustomers()
  - SEC-003: ClaimService.generateReport()

Stream C: File/Network Security
  - SEC-005: XXE prevention
  - SEC-007: Path traversal fix
  - SEC-008: SSRF prevention
  - SEC-014: Zip Slip fix

Stream D: Auth & Crypto
  - SEC-010: EncryptionUtils replacement
  - SEC-015: IDOR authorization
  - SEC-018: PasswordEncoder fix

Stream E: Test Coverage
  - Unit tests for all services
  - Integration tests for all controllers
  - Security regression tests
```

#### Step 2: Spawn Child Sessions
```
For each stream:
1. Create a child Devin session
2. Assign the appropriate skill profile:
   - Stream A: senior-developer
   - Stream B: senior-developer
   - Stream C: senior-developer
   - Stream D: senior-developer + architect
   - Stream E: qa-engineer
3. Provide context: AGENTS.md, relevant ADRs, GitHub Issues
4. Set constraints: branch naming, commit format, test requirements
```

#### Step 3: Monitor Progress
```
1. Check child session status periodically
2. Watch for merge conflicts between streams
3. Resolve conflicts by prioritizing:
   Security fixes > Auth changes > Code quality > Tests
4. Ensure each stream creates PRs to their own branch
```

#### Step 4: Integration
```
1. Merge streams in dependency order:
   A (deps) -> D (auth) -> B (SQL) -> C (files) -> E (tests)
2. Run full test suite after each merge
3. Resolve any integration issues
4. Create final integration PR to main
```

#### Step 5: Final Verification
```
1. Run: mvn clean test
2. Run: mvn clean package
3. Start application and verify all endpoints
4. Run security regression tests
5. Update vulnerability-assessment.md with results
6. Close all GitHub Issues
```

### Branch Strategy
```
main
├── remediation/phase-1-critical
│   ├── fix/sec-004-log4j-upgrade
│   ├── fix/sec-009-hardcoded-creds
│   └── fix/sec-020-dep-upgrades
├── remediation/phase-2-high
│   ├── fix/sec-001-sql-injection-claims
│   ├── fix/sec-002-sql-injection-customers
│   └── ...
└── remediation/test-coverage
    ├── test/claim-service-tests
    ├── test/customer-service-tests
    └── ...
```
