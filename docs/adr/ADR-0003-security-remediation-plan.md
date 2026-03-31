# ADR-0003: Security Remediation Plan

## Status
PROPOSED

## Date
2024-12-01

## Context
A security assessment of the claims management system has identified 20 security
vulnerabilities ranging from CRITICAL to MEDIUM severity. These must be remediated
in priority order to bring the application to an acceptable security posture.

See `docs/security/vulnerability-assessment.md` for the full assessment.

## Decision
Remediate vulnerabilities in the following priority order:

### Phase 1: Critical (Week 1)
1. **SEC-004**: Upgrade Log4j from 2.14.1 to 2.17.1+ (CVE-2021-44228)
2. **SEC-001/002/003**: Fix SQL injection by replacing string concatenation with
   parameterized queries or Spring Data JPA query methods
3. **SEC-009**: Remove all hardcoded credentials; use environment variables

### Phase 2: High (Week 2)
4. **SEC-005**: Fix XXE by disabling external entities in DocumentBuilderFactory
5. **SEC-006**: Replace ObjectInputStream with JSON deserialization
6. **SEC-007/014**: Fix path traversal and Zip Slip with path canonicalization
7. **SEC-008**: Fix SSRF with URL allowlisting and private IP blocking
8. **SEC-010**: Replace MD5/DES with bcrypt/AES-256-GCM
9. **SEC-012**: Mask PII in logs
10. **SEC-015**: Add role-based authorization to SSN lookup
11. **SEC-018**: Replace NoOpPasswordEncoder with BCryptPasswordEncoder
12. **SEC-020**: Upgrade all vulnerable dependencies

### Phase 3: Medium (Week 3)
13. **SEC-011**: Re-enable CSRF protection
14. **SEC-013**: Remove stack traces from API responses
15. **SEC-016**: Remove system-info endpoint or restrict to admin
16. **SEC-017**: Sanitize CSV output against formula injection
17. **SEC-019**: Implement rate limiting

## Consequences

### Positive
- Application reaches acceptable security posture
- Compliance with OWASP Top 10 recommendations
- Reduced attack surface

### Negative
- Breaking changes to API responses (removing sensitive fields)
- Performance overhead from encryption/hashing upgrades
- Configuration complexity increases (env vars, secrets management)

## Compliance
- [x] OWASP Top 10 2021
- [x] SOX Section 404 - Audit trail integrity
- [x] PCI DSS (if payment data involved)

## Agent Decision Log (TRiSM)
- **Requirement_ID**: REQ-SEC-001 through REQ-SEC-008
- **Agent Role**: security-analyst + architect
- **Reasoning**: Critical vulnerabilities (Log4Shell, SQLi) must be fixed first
  as they allow remote code execution. High severity items follow based on
  exploitability and data exposure risk.
- **Confidence**: HIGH
