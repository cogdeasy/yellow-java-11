# Phase 2: Implement

## Objective
Implement the feature following the approved design from Phase 1.

## Steps

1. **Create Feature Branch**
   ```bash
   git checkout -b devin/$(date +%s)-feature-name
   ```

2. **Implement Models & Repository**
   - JPA entities with proper annotations
   - Spring Data JPA repository interfaces
   - Validation annotations (JSR 380)

3. **Implement Service Layer**
   - Business logic in `@Service` classes
   - Audit event emission via `AuditClient`
   - Error handling with custom exceptions

4. **Implement Controller Layer**
   - REST endpoints matching OpenAPI spec
   - Request validation with `@Valid`
   - Proper HTTP status codes

5. **Write Unit Tests**
   - Service layer tests with Mockito
   - Controller tests with `@WebMvcTest`
   - Cover edge cases and error paths

6. **Commit with ADR Reference**
   ```
   feat(service-name): description

   Ref: ADR-NNNN
   ```

## Checklist
- [ ] Models and repositories
- [ ] Service layer with business logic
- [ ] REST controllers
- [ ] Audit event emission
- [ ] Unit tests passing
- [ ] Commit messages reference ADRs
