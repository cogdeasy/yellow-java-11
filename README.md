# Liberty Mutual Insurance Platform — Java 11

Enterprise insurance platform built with **Java 11** and **Spring Boot 2.7.x**. Implements policyholder records, policy management, premium recalculation, and compliance audit trail using spec-driven development with HITL gates, ADRs, and Testcontainers integration testing.

## Quick Start

### Prerequisites
- Java 11+
- Maven 3.8+
- Docker & Docker Compose (for local dev and integration tests)

### Local Development

```bash
# Start infrastructure (Postgres + Redis) and all services
docker-compose up -d

# Or build and run locally
mvn clean compile
mvn test          # unit tests
mvn verify        # unit + integration tests (requires Docker)
```

### Service Endpoints

| Service          | URL                    | Health Check                  |
|-----------------|------------------------|-------------------------------|
| customer-service | http://localhost:3001  | http://localhost:3001/health  |
| policy-service   | http://localhost:3002  | http://localhost:3002/health  |
| audit-service    | http://localhost:3003  | http://localhost:3003/health  |

## Architecture

```
┌──────────────────┐     ┌──────────────────┐     ┌──────────────────┐
│ customer-service │────▶│  policy-service   │     │  audit-service   │
│     :3001        │     │     :3002         │     │     :3003        │
│                  │     │                   │     │                  │
│ - CRUD customers │     │ - CRUD policies   │     │ - Ingest events  │
│ - Address change │     │ - Premium calc    │     │ - Audit trail    │
│ - Audit emission │     │ - ZIP risk data   │     │ - Enrichment     │
└────────┬─────────┘     └────────┬──────────┘     └────────┬─────────┘
         │                        │                          │
         └────────────────────────┴──────────────────────────┘
                                  │
                         ┌────────┴────────┐
                         │   PostgreSQL     │
                         │   :5432          │
                         └─────────────────┘
```

## API Reference

### Customer Service (port 3001)

| Method | Endpoint                          | Description                    |
|--------|-----------------------------------|--------------------------------|
| GET    | `/api/v1/customers`               | List customers (filters: status, state, city) |
| GET    | `/api/v1/customers/:id`           | Get customer by ID             |
| POST   | `/api/v1/customers`               | Create customer                |
| PATCH  | `/api/v1/customers/:id/address`   | Change address (triggers recalc) |

### Policy Service (port 3002)

| Method | Endpoint                              | Description                    |
|--------|---------------------------------------|--------------------------------|
| GET    | `/api/v1/policies`                    | List policies (filters: customer_id, type, status) |
| GET    | `/api/v1/policies/:id`               | Get policy by ID               |
| POST   | `/api/v1/policies`                    | Create policy with premium calc |
| POST   | `/api/v1/policies/:id/recalculate`   | Recalculate premium for address change |
| GET    | `/api/v1/zip-risk/:zipcode`          | Get ZIP risk factors           |

### Audit Service (port 3003)

| Method | Endpoint                              | Description                    |
|--------|---------------------------------------|--------------------------------|
| POST   | `/api/v2/audit-events`               | Ingest CloudEvent v1.0         |
| GET    | `/api/v2/audit-trail`                | Aggregated audit trail (enriched) |
| GET    | `/api/v2/audit-trail/:type/:id`      | Entity-specific audit trail    |
| GET    | `/api/v2/audit-trail/summary/stats`  | Audit statistics               |

## Key Business Logic

### Premium Calculation
```
premium = coverage_amount × base_rate (0.01) × zip_risk_modifier
```

Example: $500,000 coverage in Miami (33101, modifier 1.75) = $8,750/year

### Address Change Flow
1. Customer updates address → `PATCH /api/v1/customers/:id/address`
2. Customer-service emits `customer.address_changed` audit event
3. Customer-service calls policy-service to recalculate each active policy
4. Policy-service calculates new premium using ZIP risk data
5. Policy-service emits `policy.premium_recalculated` audit event
6. If new state is FL, CA, or TX → mandatory coverage re-evaluation triggered

### States Requiring Mandatory Coverage Re-evaluation
- **Florida (FL)** — Hurricane and flood zone risk
- **California (CA)** — Wildfire risk
- **Texas (TX)** — Flood and hurricane risk

## Project Structure

```
yellow-java-11/
├── pom.xml                          # Parent POM (Spring Boot 2.7.18, Java 11)
├── customer-service/                # Policyholder records
├── policy-service/                  # Policy management & premium calc
├── audit-service/                   # Compliance audit trail
├── db/
│   └── init-db.sql                  # Schema + seed data
├── docs/
│   ├── adr/                         # Architecture Decision Records
│   ├── specs/
│   │   ├── api/openapi.yml          # OpenAPI 3.0 spec
│   │   └── requirements/*.feature   # Gherkin specs
│   └── governance/                  # HITL gates, TRiSM policy
├── governance/
│   ├── kill-switch.yml              # Emergency controls
│   └── approval-gates.yml           # Required approvals
├── playbooks/                       # 5-phase workflow playbooks
├── .github/workflows/               # CI/CD pipelines
├── docker-compose.yml               # Local development
├── Dockerfile                       # Multi-stage build
├── AGENTS.md                        # Agent configuration
└── README.md                        # This file
```

## Testing

### Unit Tests
```bash
mvn test
```
Runs all `*Test.java` files (excludes `*IntegrationTest.java`).

### Integration Tests
```bash
mvn verify
```
Runs `*IntegrationTest.java` files using Testcontainers with real PostgreSQL containers. Requires Docker to be running.

## Governance

- **ADRs**: Architecture decisions documented in `docs/adr/`
- **HITL Gates**: 5 approval checkpoints defined in `docs/governance/hitl-gates.md`
- **TRiSM**: Trust, Risk, and Security Management policy in `docs/governance/trism-policy.md`
- **Kill Switch**: Emergency controls in `governance/kill-switch.yml`
- **Playbooks**: 5-phase workflow in `playbooks/`

## Technology Stack

| Component       | Version    |
|----------------|------------|
| Java           | 11 (LTS)   |
| Spring Boot    | 2.7.18     |
| PostgreSQL     | 15         |
| Redis          | 7          |
| Testcontainers | 1.19.3     |
| Maven          | 3.8+       |
| Docker         | 20+        |
