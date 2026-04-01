# AGENTS.md — Insurance Platform (Java 17)

## Overview
This repository contains two Java 17 / Spring Boot enterprise insurance systems:

1. **Liberty Mutual Platform** — Multi-module microservices (customer-service, policy-service, audit-service) built with Spring Boot 3.2.x, PostgreSQL, and Redis.
2. **Yellow Insurance Claims** — Single-module claims management system (claims-service) built with Spring Boot 3.2.x, H2 in-memory database. Contains intentional security vulnerabilities for spec-driven development demos.

## Architecture
- **Monorepo**: Maven multi-module (ADR-0001)
- **Framework**: Spring Boot 3.2.5 with Java 17
- **Database**: PostgreSQL 15 (platform), H2 in-memory (claims dev)
- **Cache**: Redis 7 (platform)
- **Event Format**: CloudEvents v1.0 (ADR-0003)

## Services

| Service          | Port | Description                            |
|-----------------|------|----------------------------------------|
| customer-service | 3001 | Policyholder records & address changes  |
| policy-service   | 3002 | Policy management & premium calculation |
| audit-service    | 3003 | Compliance audit trail                  |
| claims-service   | 8080 | Insurance claims management (demo app)  |

## Development Rules

### Mandatory
- Integration tests MUST use Testcontainers (real PostgreSQL containers) for platform services
- Commit messages MUST reference ADR-NNNN for code changes
- PRs modifying core logic MUST link to a valid ADR
- No hardcoded secrets (hard fail)
- All write operations MUST emit audit events (CloudEvents v1.0)
- Premium recalculation MUST use ZipRisk API for location-based risk factors
- Address changes in FL, CA, TX require mandatory coverage re-evaluation

### Build & Test
```bash
# Compile all modules
mvn compile

# Run unit tests only
mvn test

# Run all tests including integration (requires Docker for Testcontainers)
mvn verify

# Start local environment (platform services)
docker-compose up -d

# Run claims service standalone
mvn spring-boot:run -pl claims-service
```

### Code Conventions
- Jackson `SNAKE_CASE` for all JSON serialization
- UUID primary keys for all entities
- `OffsetDateTime` for all timestamps
- Spring Data JPA repositories (no raw SQL in application code)
- `@Valid` on all request body parameters
- Structured error responses: `{"error": "type", "message": "details"}`
- Target Java 17 compatibility

### Workflow Protocol
1. Read relevant ADR before making changes
2. Create feature branch: `devin/$(date +%s)-description`
3. Implement with tests
4. Verify: `mvn verify`
5. Create PR referencing ADR
6. Pass all HITL gates

## Key Business Logic

### Premium Calculation (ADR-0004)
```
premium = coverage_amount × 0.01 × zip_risk_modifier
```

### States Requiring Coverage Re-evaluation
- Florida (FL) — hurricane/flood risk
- California (CA) — wildfire risk
- Texas (TX) — flood/hurricane risk

### ZIP Risk Data
| ZIP   | State | Modifier | Review Required |
|-------|-------|----------|-----------------|
| 02101 | MA    | 1.10     | No              |
| 33101 | FL    | 1.75     | Yes             |
| 90001 | CA    | 1.55     | Yes             |
| 77001 | TX    | 1.60     | Yes             |
| 10001 | NY    | 1.30     | No              |
| 80201 | CO    | 1.05     | No              |

## Claims Service — Known Vulnerability Categories
This application intentionally contains these vulnerability categories for remediation demos:
1. **SQL Injection** (CWE-89) — String concatenation in native queries
2. **Log4Shell** (CVE-2021-44228) — Vulnerable Log4j 2.14.1
3. **XXE** (CWE-611) — Unprotected XML parser
4. **Insecure Deserialization** (CWE-502) — ObjectInputStream usage
5. **Path Traversal** (CWE-22) — Unsanitized file paths
6. **SSRF** (CWE-918) — Unvalidated URL fetching
7. **Weak Cryptography** (CWE-327) — MD5 hashing, DES encryption
8. **Hardcoded Credentials** (CWE-798) — Passwords in source code
9. **CSRF Disabled** (CWE-352) — Cross-site request forgery unprotected
10. **PII Exposure** (CWE-532) — SSN logged in plaintext

## Spec-Driven Development Integration
### Skill Profiles (`.agents/skills/`)
- **Architect**: Designs solutions, creates ADRs, reviews architecture
- **Senior Developer**: Implements fixes, writes code, follows TDD
- **Security Analyst**: Identifies vulnerabilities, creates remediation plans
- **QA Engineer**: Writes tests, validates fixes, ensures coverage
- **Tech Lead**: Orchestrates work, manages priorities, reviews PRs

### Constraints
1. Target Java 17 compatibility
2. All vulnerability fixes must include regression tests
3. Commit messages must reference issue IDs (SEC-NNN or CQ-NNN)
4. PRs must link to ADR for architectural changes
5. No hardcoded secrets in final remediated code
6. All write operations must emit audit events
7. Premium recalculation must use ZIP risk factor model
8. PII fields (SSN, DOB) must be encrypted at rest after remediation

## File Structure
```
yellow-java-11/
├── pom.xml                          # Parent POM (multi-module)
├── customer-service/                # Policyholder records
├── policy-service/                  # Policy management & premium calc
├── audit-service/                   # Compliance audit trail
├── claims-service/                  # Insurance claims management (demo)
│   ├── pom.xml
│   └── src/main/java/com/yellowinsurance/claims/
├── db/
│   └── init-db.sql                  # Schema + seed data
├── docs/
│   ├── adr/                         # Architecture Decision Records
│   ├── specs/
│   │   ├── api/openapi.yml          # OpenAPI 3.0 spec
│   │   └── requirements/*.feature   # Gherkin specs
│   ├── governance/                  # HITL gates, TRiSM policy
│   └── security/                    # Vulnerability assessment
├── governance/
│   ├── kill-switch.yml              # Emergency controls
│   └── approval-gates.yml           # Required approvals
├── playbooks/                       # Workflow playbooks
├── .agents/skills/                  # Role-specific skill profiles
├── .github/workflows/               # CI/CD pipelines
├── docker-compose.yml               # Local development
├── Dockerfile                       # Multi-stage build
└── README.md
```
