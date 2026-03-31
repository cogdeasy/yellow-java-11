# Software Factory 2.0: Spec-Driven Development — Java 11 Edition

> **Proof of Concept**: Agentic Engineering Blueprint demonstrating spec-driven development
> with Human-in-the-Loop (HITL) gates, Architecture Decision Records (ADRs), test-forward
> implementation, and fleet orchestration — tailored for the insurance domain.
> Implemented in **Java 11** with **Spring Boot 2.7.x**.

## Demo Scenario: Change Home Address — Policy Impact

**"Change Home Address — Policy Impact"** — trace a policyholder's address change across
multiple upstream source systems, recalculate premiums, verify coverage eligibility, and
record everything in an immutable audit trail for NAIC/SOX compliance.

This demo implements an enterprise insurance platform architecture where a **customer-service**
(Policyholder Records) and **policy-service** (Policy Platform) feed change events into an
**audit-service** (Compliance Data Store) — demonstrating every phase of the Software
Factory 2.0 blueprint.

## Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                  Policyholder Portal / API                    │
└─────────────────┬───────────────────────────────────────────┘
                  │
                  ▼
┌─────────────────────────────────────────────────────────────┐
│          audit-service — Compliance Data Store (port 8083)    │
│          GET /api/v2/audit-trail                              │
│          POST /api/v2/audit-events (CloudEvents)             │
│          ┌────────────────┐  ┌────────────────┐              │
│          │ Enriches       │  │ Enriches       │              │
│          │ Customer Data  │  │ Policy Data    │              │
│          └──────┬─────────┘  └──────┬─────────┘              │
└─────────────────┼───────────────────┼────────────────────────┘
                  │                   │
        ┌─────────┘                   └──────────┐
        ▼                                        ▼
┌────────────────────┐              ┌────────────────────┐
│  customer-service  │              │  policy-service    │
│  Policyholder Recs │              │  Policy Platform   │
│  (port 8081)       │              │  (port 8082)       │
│  /api/v1/customers │              │  /api/v1/policies  │
│  PATCH .../address │              │  POST .../recalc   │
└────────┬───────────┘              └────────┬───────────┘
         │                                   │
         └───────────┬───────────────────────┘
                     ▼
         ┌──────────────────┐    ┌──────────────────┐
         │   PostgreSQL     │    │     Redis         │
         │   (port 5432)    │    │   (port 6379)     │
         └──────────────────┘    └──────────────────┘
```

## Technology Stack

| Component | Technology |
|-----------|-----------|
| Language | Java 11 (LTS) |
| Framework | Spring Boot 2.7.x |
| Persistence | Spring Data JPA + Hibernate |
| Build | Maven (multi-module) |
| Testing | JUnit 5 + Testcontainers + Cucumber-JVM |
| Migrations | Flyway |
| API Docs | springdoc-openapi (OpenAPI 3.0) |
| Containers | Docker Compose |

## The 5-Phase Agentic Workflow

### Phase 1: Requirement Synthesis (BA Agent + HITL Gate)
- **Artifacts**: Gherkin feature specs in [`docs/specs/requirements/`](docs/specs/requirements/)
- **HITL Gate 1**: Human PO approves Gherkin scenarios before design begins
- **Playbook**: [`playbooks/01-requirement-synthesis.md`](playbooks/01-requirement-synthesis.md)

### Phase 2: Architectural Guardrails (Architect Agent + HITL Gate)
- **Artifacts**: ADR documents in [`docs/adr/`](docs/adr/)
- **HITL Gate 2**: Human Architect approves ADR before implementation begins
- **TRiSM**: Every ADR includes Agent Decision Log with reasoning

### Phase 3: Test-Forward Implementation (Dev & Tester Agents)
- **Constraint**: Integration tests use **Testcontainers** (real PostgreSQL) — **no pure mocking**
- **Pattern**: Tests written from Gherkin specs -> implementation passes tests
- **Security**: ITSO Agent scans for secrets and SBOM compliance

### Phase 4: Fleet Orchestration (Devin / Macro Agent)
- **Pattern**: Parent Devin decomposes cross-service features into child sessions
- **Context**: Service Map RAG (DeepWiki) provides dependency information

### Phase 5: Verification & Review
- **ADR Validation**: PRs must link to valid ADRs
- **Drift Detection**: Code violating ADR decisions fails the build
- **Auto-Review**: Devin Review catches bugs and pushes fixes

## Quick Start

### Prerequisites
- Java 11 (JDK)
- Maven 3.8+
- Docker & Docker Compose

### Build & Test
```bash
# Build all modules
mvn clean install

# Run all tests
mvn test

# Start infrastructure + all services
docker compose up -d

# Verify services are healthy
curl http://localhost:8081/actuator/health  # customer-service
curl http://localhost:8082/actuator/health  # policy-service
curl http://localhost:8083/actuator/health  # audit-service
```

## API Reference

See the full [OpenAPI specification](docs/specs/api/openapi.yaml).

### Customer Service (port 8081) — Policyholder Records
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/customers` | List policyholder profiles |
| GET | `/api/v1/customers/:id` | Get policyholder by ID |
| POST | `/api/v1/customers` | Create a new policyholder profile |
| PATCH | `/api/v1/customers/:id/address` | Change policyholder home address |

### Policy Service (port 8082) — Policy Platform
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/policies` | List policies |
| GET | `/api/v1/policies/:id` | Get policy by ID |
| POST | `/api/v1/policies` | Create a new policy |
| POST | `/api/v1/policies/:id/recalculate` | Recalculate premium for address change |
| GET | `/api/v1/zip-risk/:zipcode` | Get risk factors for a ZIP code |

### Audit Service (port 8083) — Compliance Data Store
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v2/audit-trail` | Aggregated audit trail with cross-service enrichment |
| GET | `/api/v2/audit-trail/:type/:id` | Audit trail for a specific entity |
| GET | `/api/v2/audit-trail/summary/stats` | Audit trail summary statistics |
| POST | `/api/v2/audit-events` | Ingest audit event (CloudEvents v1.0 format) |

## Project Structure
```
├── AGENTS.md                          # Devin/agent configuration
├── README.md                          # This file
├── pom.xml                            # Parent Maven POM
├── docs/
│   ├── adr/                           # Architecture Decision Records
│   ├── specs/
│   │   ├── requirements/              # Gherkin BDD specifications
│   │   └── api/                       # OpenAPI 3.0 specification
│   └── governance/                    # HITL gates, TRiSM, kill switch docs
├── services/
│   ├── customer-service/              # Spring Boot — Policyholder records
│   ├── policy-service/                # Spring Boot — Policy management + premium calc
│   └── audit-service/                 # Spring Boot — Compliance audit trail (ODS)
├── playbooks/                         # Agent playbooks for each phase
├── governance/                        # Kill switch and approval gate configs
├── scripts/                           # Validation and indexing scripts
└── .github/workflows/                 # CI pipelines with HITL enforcement
```

## Governance & Kill Switch

### Kill Switch
Toggle agent mode via the `AGENT_MODE_ENABLED` environment variable:
- `true`: Full agentic workflow with HITL gates
- `false`: Agents revert to assistive-only mode

See [`governance/kill-switch.yml`](governance/kill-switch.yml).

## Blueprint Proof Points

| Blueprint Concept | Demo Implementation |
|-------------------|-------------------|
| BA Agent | Gherkin specs in `docs/specs/requirements/` |
| HITL Gates | GitHub PR reviews + CI approval gates |
| ADR Generator | ADR documents with TRiSM metadata in `docs/adr/` |
| Testcontainers | Real PostgreSQL in integration tests (no mocking) |
| Fleet Orchestrator | Managed Devins playbook |
| Kill Switch | `governance/kill-switch.yml` + feature flag |
| TRiSM Traceability | Requirement -> ADR -> Code -> Test -> Audit Event chain |
| Audit Trail | `GET /api/v2/audit-trail` with cross-service enrichment |
| Compliance | NAIC, SOX, BCBS-239, GDPR proof points |
