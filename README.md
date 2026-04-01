# Insurance Platform — Java 11

This repository contains two Java 11 / Spring Boot enterprise insurance systems:

1. **Liberty Mutual Platform** — Multi-module microservices for policyholder records, policy management, premium recalculation, and compliance audit trail. Built with Spring Boot 2.7.x, PostgreSQL, and Redis.
2. **Yellow Insurance Claims** — A fully functional but intentionally vulnerable claims management system for demonstrating spec-driven development workflows, security remediation, and agentic engineering patterns.

## Quick Start

### Prerequisites
- Java 11+
- Maven 3.8+
- Docker & Docker Compose (for local dev and integration tests)

### Local Development

```bash
# Start infrastructure (Postgres + Redis) and platform services
docker-compose up -d

# Or build and run locally
mvn clean compile
mvn test          # unit tests
mvn verify        # unit + integration tests (requires Docker)

# Run claims service standalone
mvn spring-boot:run -pl claims-service
```

### Service Endpoints

| Service          | URL                    | Health Check                  |
|-----------------|------------------------|-------------------------------|
| customer-service | http://localhost:3001  | http://localhost:3001/health  |
| policy-service   | http://localhost:3002  | http://localhost:3002/health  |
| audit-service    | http://localhost:3003  | http://localhost:3003/health  |
| claims-service   | http://localhost:8080  | http://localhost:8080/api/v1/admin/health |

## Architecture

### Platform Services

customer-service :3001 -> policy-service :3002 -> audit-service :3003 -> PostgreSQL :5432

### Claims Service

REST API (Spring Boot 2.5) on port 8080 with Claims, Customer, Policy, Document, Admin controllers over JPA/Hibernate with H2 in-memory DB.

## Key Business Logic

### Premium Calculation
premium = coverage_amount x base_rate (0.01) x zip_risk_modifier

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
├── claims-service/                  # Insurance claims management (demo app)
├── db/                              # Database init scripts
├── docs/                            # ADRs, specs, governance, security
├── governance/                      # Kill switch, approval gates
├── playbooks/                       # Workflow playbooks
├── .agents/skills/                  # Role-specific skill profiles
├── .github/workflows/               # CI/CD pipelines
├── docker-compose.yml               # Local development
├── Dockerfile                       # Multi-stage build
└── README.md
```

## Testing

```bash
mvn test    # Unit tests
mvn verify  # Unit + integration tests (requires Docker)
```

## Technology Stack

| Component       | Version    | Used By         |
|----------------|------------|-----------------|
| Java           | 11 (LTS)   | All             |
| Spring Boot    | 2.7.18     | Platform        |
| Spring Boot    | 2.5.14     | Claims          |
| PostgreSQL     | 15         | Platform        |
| H2             | In-memory  | Claims (dev)    |
| Redis          | 7          | Platform        |
| Testcontainers | 1.19.3     | Platform tests  |
| Maven          | 3.8+       | All             |
