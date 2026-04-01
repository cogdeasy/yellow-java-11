# Yellow Insurance Claims Management System (Java 11)

> **Demo Application**: A fully functional but intentionally vulnerable Java 11 / Spring Boot
> insurance claims management system. This project serves as a realistic codebase for
> demonstrating **Spec-Driven Development** workflows, security remediation, code quality
> improvements, and agentic engineering patterns.

## Purpose

This repository is designed to showcase how a **Software Factory 2.0** workflow handles a
legacy Java 11 application with real-world issues:

1. **Security vulnerabilities** that need remediation (OWASP Top 10)
2. **Code quality issues** that need refactoring
3. **Missing test coverage** that needs to be built
4. **Architectural concerns** that need ADR-driven resolution
5. **Compliance gaps** that need governance enforcement

The companion repository [`yellow-spec-dd`](https://github.com/cogdeasy/yellow-spec-dd)
contains the spec-driven development framework, playbooks, and governance structure.

## Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                     REST API (Spring Boot 2.5)                   │
│                        Port 8080                                 │
├──────────┬──────────┬──────────┬──────────┬────────────────────── │
│  Claims  │ Customer │  Policy  │ Document │  Admin               │
│  /api/v1 │ /api/v1  │ /api/v1  │ /api/v1  │  /api/v1             │
│  /claims │/customers│/policies │/documents│  /admin               │
├──────────┴──────────┴──────────┴──────────┴────────────────────── │
│                    Service Layer                                  │
│  ClaimService | CustomerService | PolicyService | DocumentService │
│  NotificationService | ReportService                              │
├──────────────────────────────────────────────────────────────────-│
│                    JPA / Hibernate                                │
│                    H2 In-Memory DB                                │
└──────────────────────────────────────────────────────────────────-┘
```

## Quick Start

### Prerequisites
- Java 11+ (tested with Java 11 and 17)
- Maven 3.6+

### Build & Run
```bash
# Build the project
mvn clean package -DskipTests

# Run the application
mvn spring-boot:run

# Or run the JAR directly
java -jar target/claims-management-1.0.0-SNAPSHOT.jar
```

### Access the Application
- **API Base URL**: http://localhost:8080/api/v1
- **H2 Console**: http://localhost:8080/h2-console
- **Health Check**: http://localhost:8080/api/v1/admin/health

### Default Credentials
| Username | Password | Role |
|----------|----------|------|
| admin | admin123! | ADMIN, USER |
| adjuster | adjust123 | USER |
| viewer | view123 | USER |

### API Endpoints

#### Claims
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/claims` | List all claims |
| GET | `/api/v1/claims/{id}` | Get claim by ID |
| POST | `/api/v1/claims` | Create a new claim |
| PATCH | `/api/v1/claims/{id}/status` | Update claim status |
| GET | `/api/v1/claims/search` | Search claims |
| GET | `/api/v1/claims/statistics` | Get claim statistics |
| GET | `/api/v1/claims/reports` | Generate reports |

#### Customers
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/customers` | List all customers |
| GET | `/api/v1/customers/{id}` | Get customer by ID |
| POST | `/api/v1/customers` | Create customer |
| PUT | `/api/v1/customers/{id}` | Update customer |
| GET | `/api/v1/customers/search` | Search customers |
| GET | `/api/v1/customers/lookup/ssn/{ssn}` | Lookup by SSN |

#### Policies
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/policies` | List policies |
| GET | `/api/v1/policies/{id}` | Get policy by ID |
| POST | `/api/v1/policies` | Create policy |
| POST | `/api/v1/policies/{id}/recalculate` | Recalculate premium |
| GET | `/api/v1/policies/zip-risk/{zipcode}` | Get ZIP risk factors |

#### Documents
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/documents/upload` | Upload document |
| GET | `/api/v1/documents/{id}/download` | Download document |
| POST | `/api/v1/documents/import` | Import from URL |
| GET | `/api/v1/documents/claim/{claimId}` | Documents by claim |
| DELETE | `/api/v1/documents/{id}` | Delete document |

#### Admin
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/admin/audit-logs` | View audit logs |
| GET | `/api/v1/admin/reports/claims-csv` | Export CSV report |
| POST | `/api/v1/admin/import/xml` | Import XML data |
| GET | `/api/v1/admin/system-info` | System information |
| GET | `/api/v1/admin/health` | Health check |

## Known Issues & Vulnerabilities

This application **intentionally** contains the following issues for demonstration purposes:

### Security Vulnerabilities (OWASP Top 10)
| ID | Category | Location | Severity |
|----|----------|----------|----------|
| SEC-001 | SQL Injection | ClaimService.searchClaims() | CRITICAL |
| SEC-002 | SQL Injection | CustomerService.searchCustomers() | CRITICAL |
| SEC-003 | SQL Injection | ClaimService.generateReport() | CRITICAL |
| SEC-004 | Log4Shell (CVE-2021-44228) | Log4j 2.14.1 dependency | CRITICAL |
| SEC-005 | XXE Injection | XmlParser.parseXml() | HIGH |
| SEC-006 | Insecure Deserialization | NotificationService | HIGH |
| SEC-007 | Path Traversal | DocumentService upload/download | HIGH |
| SEC-008 | SSRF | DocumentService.importFromUrl() | HIGH |
| SEC-009 | Hardcoded Credentials | SecurityConfig, application.yml | HIGH |
| SEC-010 | Weak Cryptography (MD5/DES) | EncryptionUtils | HIGH |
| SEC-011 | CSRF Disabled | SecurityConfig | MEDIUM |
| SEC-012 | PII Exposure (SSN in logs) | CustomerService | HIGH |
| SEC-013 | Stack Trace Exposure | GlobalExceptionHandler | MEDIUM |
| SEC-014 | Zip Slip | FileUtils.extractZip() | HIGH |
| SEC-015 | IDOR | Customer SSN lookup endpoint | HIGH |
| SEC-016 | Information Disclosure | AdminController.getSystemInfo() | MEDIUM |
| SEC-017 | CSV Injection | ReportService.generateClaimsCsv() | MEDIUM |
| SEC-018 | NoOpPasswordEncoder | SecurityConfig | HIGH |
| SEC-019 | Missing Rate Limiting | All endpoints | MEDIUM |
| SEC-020 | Insecure Dependencies | pom.xml (multiple CVEs) | HIGH |

### Code Quality Issues
| ID | Category | Location |
|----|----------|----------|
| CQ-001 | God Class | ClaimService (search, report, cache, audit) |
| CQ-002 | No Unit Tests | 0% test coverage |
| CQ-003 | Magic Numbers | Throughout services |
| CQ-004 | Thread Safety | ClaimService.claimCache |
| CQ-005 | Resource Leaks | DocumentService, NotificationService |
| CQ-006 | Dead Code | ClaimService.processClaimBatch() |
| CQ-007 | Catch-All Exceptions | Multiple services |
| CQ-008 | Missing Validation | DTOs, Controllers |
| CQ-009 | No Foreign Keys | Database schema |
| CQ-010 | Missing Indexes | Database schema |
| CQ-011 | Business Logic in Controllers | ClaimController |
| CQ-012 | Copy-Paste Patterns | Search methods in services |
| CQ-013 | Floating Point Money | PolicyService.recalculatePremium() |
| CQ-014 | Deprecated APIs | WebSecurityConfigurerAdapter |
| CQ-015 | Missing Audit Trail | Not tracking actual users |

## Spec-Driven Development Workflow

This project is designed to be improved through the spec-dd workflow:

1. **Security Analyst** reviews vulnerabilities and creates remediation specs
2. **Architect** designs solutions and creates ADRs
3. **Developer** implements fixes following test-forward methodology
4. **QA Engineer** validates fixes and writes comprehensive tests
5. **Tech Lead** orchestrates the work across roles

See the `.agents/skills/` directory for role-specific skill profiles and the
`playbooks/` directory for workflow playbooks.

## Project Structure
```
├── pom.xml                           # Maven build (Spring Boot 2.5.14, Java 11)
├── README.md                         # This file
├── AGENTS.md                         # Devin/agent configuration
├── src/
│   ├── main/
│   │   ├── java/com/yellowinsurance/claims/
│   │   │   ├── ClaimsApplication.java
│   │   │   ├── controller/           # REST controllers
│   │   │   ├── service/              # Business logic
│   │   │   ├── repository/           # Data access (JPA)
│   │   │   ├── model/                # JPA entities + DTOs
│   │   │   ├── config/               # Security, Web config
│   │   │   ├── util/                 # Utilities (crypto, XML, file)
│   │   │   └── exception/            # Error handling
│   │   └── resources/
│   │       ├── application.yml       # Configuration
│   │       ├── schema.sql            # Database schema
│   │       ├── data.sql              # Seed data
│   │       └── log4j2.xml            # Logging config
│   └── test/                         # Tests (minimal - intentional gap)
├── docs/
│   ├── specs/requirements/           # Gherkin BDD specifications
│   ├── specs/api/                    # OpenAPI specification
│   ├── adr/                          # Architecture Decision Records
│   └── security/                     # Security assessment
├── .agents/skills/                   # Role-specific skill profiles
├── playbooks/                        # Workflow playbooks
└── .github/workflows/                # CI pipeline
```

## Insurance Domain Context
- **Policy Types**: HOME, AUTO, LIFE, HEALTH
- **Claims Types**: WATER_DAMAGE, WILDFIRE, COLLISION, WIND_DAMAGE, FLOOD, THEFT, MOLD
- **High-Risk States**: FL (hurricane), CA (wildfire), TX (tornado/flood), LA (hurricane/flood)
- **Compliance**: SOX audit trails, PII protection, NAIC regulations
- **Premium Calculation**: ZIP-based risk factor model
