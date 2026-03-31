# ADR-0001: Monorepo Structure

## Status
Accepted

## Date
2025-01-15

## Context
We need to decide how to structure the Liberty Mutual Insurance Platform codebase. The platform consists of three microservices (customer-service, policy-service, audit-service) that share common dependencies, database infrastructure, and deployment tooling.

## Decision
We will use a Maven multi-module monorepo with a shared parent POM. Each service is a separate Maven module with its own `pom.xml` inheriting from the parent. All services share:
- A single PostgreSQL database with separate tables
- Common dependency versions managed in the parent POM
- Shared build and test configuration
- A single Docker Compose file for local development

## Consequences
- **Positive**: Simplified dependency management, atomic cross-service changes, unified CI/CD
- **Positive**: Single `mvn verify` validates all services
- **Negative**: Larger checkout size, all services rebuild on parent POM changes
- **Mitigation**: Maven's incremental build and CI caching minimize rebuild overhead
