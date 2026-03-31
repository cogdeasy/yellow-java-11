# AGENTS.md - Devin Configuration for Spec-Driven Development (Java 11 Edition)

## Project Overview
This is a **Software Factory 2.0** demo showcasing spec-driven development with
agentic workflows, HITL gates, ADR governance, and test-forward implementation —
tailored for the insurance domain, implemented in **Java 11** with Spring Boot.

## Architecture
- **Multi-module Maven project** with 3 services representing upstream source systems
- **customer-service** (port 8081): Policyholder Records source system
- **policy-service** (port 8082): Policy Platform source system (premium calc, coverage)
- **audit-service** (port 8083): Compliance Data Store (ODS) — aggregated audit trail
- **PostgreSQL** for persistence, **Redis** for caching
- **Docker Compose** for local development
- **Demo Scenario**: "Change Home Address — Policy Impact" across source systems

## Technology Stack
- **Java 11** (LTS)
- **Spring Boot 2.7.x** (last major line with Java 11 support)
- **Spring Data JPA** with Hibernate for persistence
- **Maven** for build management (multi-module)
- **Testcontainers** for integration tests (real PostgreSQL containers)
- **JUnit 5** + **AssertJ** for testing
- **Cucumber-JVM** for BDD/Gherkin test execution
- **Flyway** for database migrations
- **OpenAPI 3.0** (springdoc-openapi) for API documentation

## Domain Context (Insurance)
- Address changes affect policy premiums (new ZIP = new risk factors)
- Coverage eligibility must be re-evaluated (flood zone, wildfire zone, hurricane zone)
- States FL, CA, TX require mandatory coverage re-evaluation on address change
- Premium recalculation uses the ZipRisk API at `/api/v1/zip-risk/{zipcode}`
- All address changes must be logged with before/after snapshots for SOX compliance
- The audit-service expects events in CloudEvents v1.0 format
- Compliance requirements: NAIC Model Audit Rule, SOX, BCBS-239, GDPR

## Key Commands
```bash
# Build all modules
mvn clean install

# Run all tests
mvn test

# Run unit tests only
mvn test -Dgroups="unit"

# Run integration tests (requires Docker for Testcontainers)
mvn test -Dgroups="integration"

# Run BDD/Cucumber tests
mvn test -Dgroups="bdd"

# Start all services locally
docker compose up -d

# Validate ADR documents
./scripts/validate-adr.sh

# Validate Gherkin specifications
./scripts/validate-specs.sh
```

## Agentic Workflow Protocol
When working on this codebase, follow the 5-phase workflow:

### Phase 1: Requirement Synthesis
- Read the Gherkin specs in `docs/specs/requirements/`
- New features require a `.feature` file with HITL Gate 1 approval
- Playbook: `playbooks/01-requirement-synthesis.md`

### Phase 2: Architecture Review
- Check ADRs in `docs/adr/` before implementing
- New architectural decisions require an ADR with HITL Gate 2 approval
- Playbook: `playbooks/02-architecture-review.md`

### Phase 3: Test-Forward Development
- Write integration tests FIRST using Testcontainers
- **NO PURE MOCKING** for DB or external API interactions
- Reference ADR-NNNN in commit messages
- Playbook: `playbooks/03-test-forward-development.md`

### Phase 4: Fleet Orchestration
- For cross-service changes, use the fleet orchestration playbook
- Query DeepWiki for service dependencies
- Playbook: `playbooks/04-fleet-orchestration.md`

### Phase 5: Verification
- Ensure ADR linkage in PRs
- Verify Testcontainers usage (no mocking)
- Playbook: `playbooks/05-verification-review.md`

## Constraints
1. Integration tests MUST use Testcontainers (real PostgreSQL containers)
2. Commit messages MUST reference ADR-NNNN for code changes
3. PRs modifying core logic MUST link to a valid ADR
4. No hardcoded secrets (ITSO Agent hard fail)
5. All write operations MUST emit audit events (CloudEvents v1.0)
6. Premium recalculation MUST use ZipRisk API for location-based risk factors
7. Address changes in FL, CA, TX require mandatory coverage re-evaluation

## File Structure
```
docs/adr/          - Architecture Decision Records (TRiSM)
docs/specs/        - Gherkin requirements + OpenAPI specs
docs/governance/   - HITL gates, TRiSM policy, kill switch docs
services/          - Spring Boot microservice implementations
playbooks/         - Agent playbooks for each phase
scripts/           - Validation and indexing scripts
governance/        - Kill switch and approval gate configs
.github/workflows/ - CI pipelines with HITL enforcement
```
