# Playbook: Security Assessment (Security Analyst)

## Phase 1 of the Remediation Workflow

### Purpose
Perform a comprehensive security assessment of the codebase, catalog all
vulnerabilities, and create prioritized remediation plans.

### Prerequisites
- Access to the codebase
- Knowledge of OWASP Top 10 (2021)
- Familiarity with Java/Spring Boot security patterns

### Steps

#### Step 1: Dependency Scan
```
1. Review pom.xml for all dependencies and their versions
2. Cross-reference against NVD/GitHub Advisory Database
3. Identify dependencies with known CVEs
4. Document: dependency name, version, CVE IDs, severity
5. Identify safe upgrade versions for each
```

#### Step 2: Source Code Review
```
1. Scan all .java files for vulnerability patterns:
   - String concatenation in SQL (grep for "\" +" near "SELECT|INSERT|UPDATE|DELETE")
   - ObjectInputStream usage (insecure deserialization)
   - MessageDigest.getInstance("MD5") (weak hashing)
   - Cipher.getInstance("DES") (weak encryption)
   - new URL() followed by openStream() (SSRF)
   - DocumentBuilderFactory without security features (XXE)
   - Hardcoded strings matching password/key/secret patterns
   - System.getProperty/getenv in API responses (info disclosure)
2. Review application.yml for misconfigurations
3. Review SecurityConfig.java for auth weaknesses
4. Review logging statements for PII exposure
```

#### Step 3: Create Vulnerability Report
```
1. Update docs/security/vulnerability-assessment.md
2. Assign CWE classifications
3. Estimate CVSS scores
4. Prioritize: CRITICAL > HIGH > MEDIUM > LOW
5. Write recommended fix for each vulnerability
```

#### Step 4: Create Gherkin Security Specs
```
1. For each vulnerability, write Gherkin scenario in:
   docs/specs/requirements/security-hardening.feature
2. Tag with @security and severity level
3. Include exploit prevention and fix validation scenarios
```

#### Step 5: Create GitHub Issues
```
1. Create one GitHub Issue per vulnerability (SEC-NNN)
2. Label: severity, security, assigned-role, phase
3. Include: description, location, fix recommendation, acceptance criteria
```

### Output Artifacts
- Updated `docs/security/vulnerability-assessment.md`
- Updated `docs/specs/requirements/security-hardening.feature`
- GitHub Issues for each vulnerability
