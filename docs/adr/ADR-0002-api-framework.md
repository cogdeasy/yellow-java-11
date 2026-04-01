# ADR-0002: API Framework — Spring Boot 2.7.x with Java 11

## Status
Accepted

## Date
2025-01-15

## Context
We need a mature, well-supported Java web framework for building RESTful microservices. The platform targets Java 11 LTS for broad enterprise compatibility.

## Decision
We will use Spring Boot 2.7.18 (the last 2.x release) with Java 11. Key choices:
- **Spring Data JPA** with Hibernate for database access
- **Spring Web MVC** for REST endpoints
- **Spring Validation** (JSR 380) for request validation
- **Jackson** with `SNAKE_CASE` naming strategy for JSON serialization
- **SpringDoc OpenAPI** for automatic API documentation

## Consequences
- **Positive**: Mature ecosystem, extensive documentation, large talent pool
- **Positive**: Spring Boot 2.7.x is the last version supporting Java 11
- **Negative**: Spring Boot 3.x requires Java 17+, limiting future upgrades without a Java version bump
- **Mitigation**: The codebase is structured for easy migration to Spring Boot 3.x when Java 17 is adopted
