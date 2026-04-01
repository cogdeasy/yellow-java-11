# Playbook: Architecture Review (Architect)

## Phase 2 of the Remediation Workflow

### Purpose
Review the current architecture, design remediation solutions, and create
Architecture Decision Records (ADRs) for all significant changes.

### Prerequisites
- Security assessment complete (Playbook 01)
- Knowledge of Spring Boot architecture patterns
- Understanding of insurance domain requirements

### Steps

#### Step 1: Review Current Architecture
```
1. Read AGENTS.md for project context
2. Read all existing ADRs in docs/adr/
3. Review the layered architecture:
   - Controllers (request handling)
   - Services (business logic)
   - Repositories (data access)
   - Models (domain entities)
4. Identify architectural anti-patterns:
   - God classes (ClaimService doing too much)
   - Missing separation of concerns
   - Tight coupling between layers
   - Missing abstraction layers
```

#### Step 2: Design Security Remediation Architecture
```
For each critical/high vulnerability:
1. Determine if fix requires architectural change or just code fix
2. If architectural: create ADR with alternatives analysis
3. Design patterns to prevent recurrence:
   - Repository pattern for all DB access (no raw SQL)
   - Input validation layer (DTOs with validation annotations)
   - Security middleware (rate limiting, auth checks)
   - Encryption service abstraction
```

#### Step 3: Create ADRs
```
1. Use docs/adr/template.md as base
2. Key ADRs needed:
   - ADR-0004: SQL Injection Prevention Strategy
   - ADR-0005: Encryption and Key Management
   - ADR-0006: API Security Architecture
   - ADR-0007: Logging and Audit Architecture
   - ADR-0008: File Upload Security Architecture
3. Include TRiSM metadata in each ADR
4. Set status to PROPOSED
5. Open PRs for human review (HITL Gate 2)
```

#### Step 4: API Contract Review
```
1. Review docs/specs/api/openapi.yaml
2. Identify API changes needed for security:
   - Remove sensitive fields from responses
   - Add validation constraints
   - Add proper error response schemas
   - Add authentication requirements
3. Update OpenAPI spec with changes
```

### Output Artifacts
- ADR documents in `docs/adr/`
- Updated OpenAPI specification
- Architecture diagrams (if needed)
