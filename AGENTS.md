# AGENTS.md — Liberty Mutual Insurance Platform (Java 11)

## Overview
This is a Java 11 / Spring Boot 2.7.x enterprise insurance platform implementing policyholder records, policy management, premium recalculation, and compliance audit trail. It mirrors the TypeScript `yellow-spec-dd` and `yellow-town` reference implementations.

## Architecture
- **Monorepo**: Maven multi-module (ADR-0001)
- **Framework**: Spring Boot 2.7.18 with Java 11 (ADR-0002)
- **Database**: PostgreSQL 15 (shared across services)
- **Cache**: Redis 7
- **Event Format**: CloudEvents v1.0 (ADR-0003)

## Services

| Service          | Port | Description                           |
|-----------------|------|---------------------------------------|
| customer-service | 3001 | Policyholder records & address changes |
| policy-service   | 3002 | Policy management & premium calculation|
| audit-service    | 3003 | Compliance audit trail                 |

## Development Rules

### Mandatory
- Integration tests MUST use Testcontainers (real PostgreSQL containers)
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

# Start local environment
docker-compose up -d
```

### Code Conventions
- Jackson `SNAKE_CASE` for all JSON serialization
- UUID primary keys for all entities
- `OffsetDateTime` for all timestamps
- Spring Data JPA repositories (no raw SQL in application code)
- `@Valid` on all request body parameters
- Structured error responses: `{"error": "type", "message": "details"}`

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
