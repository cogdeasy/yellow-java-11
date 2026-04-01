# ADR-0001: Spring Boot 2.5 with Java 11

## Status
ACCEPTED

## Date
2024-01-15

## Context
The claims management system was initially developed targeting Java 11 LTS and
Spring Boot 2.5.14. Java 11 was chosen as the minimum supported version due to
enterprise deployment constraints where many production environments still run
Java 11. Spring Boot 2.5.x was the latest 2.x line at the time of project inception.

Note: Spring Boot 2.5.x has reached end of life. A future ADR will address the
upgrade path to Spring Boot 3.x / Java 17+.

## Decision
Use Java 11 as the compilation target and Spring Boot 2.5.14 as the framework
version. Maven is the build tool.

Key configuration:
- `maven.compiler.source=11` / `maven.compiler.target=11`
- Parent POM: `spring-boot-starter-parent:2.5.14`
- H2 for development, PostgreSQL for production

## Consequences

### Positive
- Java 11 LTS ensures broad compatibility with enterprise environments
- Spring Boot 2.5 provides mature ecosystem with extensive documentation
- H2 enables zero-config local development

### Negative
- Spring Boot 2.5 is EOL - no security patches
- Java 11 misses performance improvements in Java 17+ (records, sealed classes, etc.)
- Some newer libraries require Java 17+ minimum
- `WebSecurityConfigurerAdapter` is deprecated (removed in Spring Security 6)

### Risks
- Known CVEs in Spring Boot 2.5.x dependencies
- Log4j 2.14.1 bundled with critical CVE-2021-44228
- Must plan upgrade to Spring Boot 3.x / Java 17

## Alternatives Considered
1. **Java 17 + Spring Boot 3.x**: Best practice but breaks compatibility with
   enterprise Java 11 environments. Planned as future migration (ADR-NNNN).
2. **Java 11 + Spring Boot 2.7.x**: Marginal improvement; 2.7 also approaching EOL.

## Compliance
- [x] NAIC Model Audit Rule - N/A for framework choice
- [x] SOX Section 404 - Framework must support audit logging (verified)

## Agent Decision Log (TRiSM)
- **Requirement_ID**: REQ-ARCH-001
- **Agent Role**: architect
- **Reasoning**: Enterprise compatibility with Java 11 is a hard constraint from
  the deployment environment. Spring Boot 2.5.14 was the stable version at project
  inception. Upgrade to 3.x is planned but requires separate ADR.
- **Confidence**: HIGH
