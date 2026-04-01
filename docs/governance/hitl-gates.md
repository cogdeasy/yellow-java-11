# Human-in-the-Loop (HITL) Gates

## Overview
HITL gates are mandatory approval checkpoints in the development workflow. No code reaches production without passing through these gates.

## Gate Definitions

### Gate 1: Design Review
- **Trigger**: New feature or architectural change
- **Approver**: Tech Lead
- **Artifacts Required**: ADR document, API specification
- **Criteria**: Alignment with existing architecture, security review, scalability assessment

### Gate 2: Code Review
- **Trigger**: Pull request created
- **Approver**: At least 1 peer reviewer
- **Artifacts Required**: Passing CI, test coverage report
- **Criteria**: Code quality, test coverage ≥ 80%, no security vulnerabilities

### Gate 3: Integration Validation
- **Trigger**: All unit and integration tests pass
- **Approver**: QA Lead
- **Artifacts Required**: Integration test results, Testcontainers logs
- **Criteria**: All endpoints functional, audit events properly emitted, premium calculations correct

### Gate 4: Compliance Check
- **Trigger**: Pre-deployment
- **Approver**: Compliance Officer
- **Artifacts Required**: Audit trail verification, Gherkin spec compliance
- **Criteria**: All write operations emit audit events, coverage re-evaluation triggers correctly for FL/CA/TX

### Gate 5: Production Release
- **Trigger**: All previous gates passed
- **Approver**: Release Manager
- **Artifacts Required**: Signed-off gates 1-4, rollback plan
- **Criteria**: Kill switch tested, health checks verified, monitoring configured

## Gate Status Tracking
Gates are tracked via PR labels:
- `gate-1-approved`: Design review passed
- `gate-2-approved`: Code review passed
- `gate-3-approved`: Integration validation passed
- `gate-4-approved`: Compliance check passed
- `gate-5-approved`: Ready for production
