# AGENTS.md - Devin Configuration for Yellow Insurance Claims (Java 11)

## Project Overview
This is a **Java 11 / Spring Boot 2.5** insurance claims management system that serves
as a realistic demo application for the **Software Factory 2.0** spec-driven development
workflow. The application is fully functional but contains intentional security
vulnerabilities, code quality issues, and architectural concerns that demonstrate
how agentic workflows handle legacy codebase remediation.

## Technology Stack
- **Language**: Java 11
- **Framework**: Spring Boot 2.5.14
- **Build**: Maven 3.6+
- **Database**: H2 (in-memory, dev) / PostgreSQL (production target)
- **ORM**: Hibernate / Spring Data JPA
- **Security**: Spring Security (HTTP Basic)
- **Logging**: Log4j2 (intentionally vulnerable version 2.14.1)
- **API Docs**: Springfox Swagger 3.0

## Key Commands
```bash
# Build the project
mvn clean compile

# Run tests
mvn test

# Package (skip tests)
mvn clean package -DskipTests

# Run the application
mvn spring-boot:run

# Run with specific profile
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

## Architecture
- Single Spring Boot application with layered architecture
- Controllers -> Services -> Repositories -> H2 Database
- 5 domain entities: Claim, Customer, Policy, Document, AuditLog
- REST API with Basic Authentication
- H2 in-memory database with seed data

## Domain Context (Insurance)
- **Policy Types**: HOME, AUTO, LIFE, HEALTH
- **Claim Types**: WATER_DAMAGE, WILDFIRE, COLLISION, WIND_DAMAGE, FLOOD, THEFT, MOLD
- **High-Risk States**: FL (hurricane), CA (wildfire/earthquake), TX (tornado/flood)
- **Premium Recalculation**: ZIP code-based risk factor model
- **Compliance**: SOX audit trail, PII protection (SSN), NAIC regulatory requirements
- Address changes in FL, CA, TX require mandatory coverage re-evaluation
- All state changes must produce audit log entries

## Known Vulnerability Categories
This application intentionally contains these vulnerability categories for remediation demos:
1. **SQL Injection** (CWE-89) - String concatenation in native queries
2. **Log4Shell** (CVE-2021-44228) - Vulnerable Log4j 2.14.1
3. **XXE** (CWE-611) - Unprotected XML parser
4. **Insecure Deserialization** (CWE-502) - ObjectInputStream usage
5. **Path Traversal** (CWE-22) - Unsanitized file paths
6. **SSRF** (CWE-918) - Unvalidated URL fetching
7. **Weak Cryptography** (CWE-327) - MD5 hashing, DES encryption
8. **Hardcoded Credentials** (CWE-798) - Passwords in source code
9. **CSRF Disabled** (CWE-352) - Cross-site request forgery unprotected
10. **PII Exposure** (CWE-532) - SSN logged in plaintext

## Spec-Driven Development Integration
This project is designed to be improved through role-based agentic workflows:

### Skill Profiles (`.agents/skills/`)
- **Architect**: Designs solutions, creates ADRs, reviews architecture
- **Senior Developer**: Implements fixes, writes code, follows TDD
- **Security Analyst**: Identifies vulnerabilities, creates remediation plans
- **QA Engineer**: Writes tests, validates fixes, ensures coverage
- **Tech Lead**: Orchestrates work, manages priorities, reviews PRs

### Workflow Phases
1. **Assessment**: Security analyst scans and catalogs all vulnerabilities
2. **Architecture**: Architect creates ADRs for each remediation approach
3. **Implementation**: Developer implements fixes test-forward
4. **Verification**: QA validates fixes, security analyst re-scans
5. **Review**: Tech lead reviews and approves changes

## Constraints
1. Target Java 11 compatibility - do not use Java 12+ features
2. Keep Spring Boot 2.5.x until explicit upgrade decision (requires ADR)
3. All vulnerability fixes must include regression tests
4. Commit messages must reference issue IDs (SEC-NNN or CQ-NNN)
5. PRs must link to ADR for architectural changes
6. No hardcoded secrets in final remediated code
7. All write operations must emit audit events
8. Premium recalculation must use ZIP risk factor model
9. PII fields (SSN, DOB) must be encrypted at rest after remediation

## File Structure
```
src/main/java/com/yellowinsurance/claims/
├── ClaimsApplication.java          # Main entry point
├── controller/                     # REST API endpoints
├── service/                        # Business logic
├── repository/                     # Data access layer
├── model/                          # JPA entities
│   └── dto/                        # Data transfer objects
├── config/                         # Security, web configuration
├── util/                           # Cryptography, XML, file utilities
└── exception/                      # Global error handling

docs/
├── specs/requirements/             # Gherkin BDD specs
├── specs/api/                      # OpenAPI 3.0 spec
├── adr/                            # Architecture Decision Records
└── security/                       # Vulnerability assessment

.agents/skills/                     # Role-specific Devin skill profiles
playbooks/                          # Workflow automation playbooks
```
