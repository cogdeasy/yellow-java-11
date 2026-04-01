# ADR-0002: H2 In-Memory Database for Development

## Status
ACCEPTED

## Date
2024-01-15

## Context
The development environment needs a database that requires zero configuration,
starts with the application, and contains seed data for testing. Production
will target PostgreSQL, so the development database should be SQL-compatible.

## Decision
Use H2 in-memory database for local development with `spring.jpa.hibernate.ddl-auto=update`.
Schema is defined in `schema.sql` and seed data in `data.sql`. The H2 web console
is enabled at `/h2-console` for development debugging.

**Important**: The H2 console MUST be disabled in production profiles.

## Consequences

### Positive
- Zero-config startup - no external database needed
- Fast test execution with in-memory storage
- SQL compatibility with PostgreSQL for most operations
- Built-in web console for debugging

### Negative
- H2 SQL dialect differs from PostgreSQL in edge cases
- In-memory data is lost on restart
- H2 console is a security risk if exposed in production
- `ddl-auto=update` can cause schema drift

### Risks
- Developers may write H2-specific SQL that fails on PostgreSQL
- H2 console exposed in production would allow direct DB manipulation
- Schema drift between `schema.sql` and Hibernate-generated DDL

## Alternatives Considered
1. **Docker PostgreSQL**: More production-like but requires Docker for all developers
2. **Testcontainers only**: Good for tests but inconvenient for local development
3. **SQLite**: Less SQL-compatible than H2 for PostgreSQL migration

## Agent Decision Log (TRiSM)
- **Requirement_ID**: REQ-ARCH-002
- **Agent Role**: architect
- **Reasoning**: H2 provides the fastest development experience with zero setup.
  The trade-off of SQL dialect differences is acceptable for a demo application.
- **Confidence**: HIGH
