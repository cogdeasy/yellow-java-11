# TRiSM (Trust, Risk, Security Management) Policy

## Overview
Every agent decision is traceable from business requirement through to deployed code
and runtime audit events. This policy defines the traceability chain.

## Traceability Chain
```
Business Requirement (English)
    ↓ Requirement_ID: REQ-{DOMAIN}-{NNN}
Gherkin Specification (BDD)
    ↓ linked via Requirement_ID
Architecture Decision Record (ADR)
    ↓ ADR-NNNN referenced in commits
Integration Tests (Testcontainers)
    ↓ tests implement Gherkin scenarios
Implementation (Java 11 / Spring Boot)
    ↓ CI/CD pipeline
Audit Trail (Runtime)
    ↓ CloudEvents v1.0
Compliance Report
```

## Requirements
1. Every Gherkin scenario MUST have a `Requirement_ID`
2. Every ADR MUST reference the originating `Requirement_ID`
3. Every code commit MUST reference an `ADR-NNNN`
4. Every write operation MUST emit a CloudEvents v1.0 audit event
5. Agent Decision Logs MUST be included in ADR documents

## Compliance Mapping
| Standard | How We Comply |
|----------|--------------|
| NAIC Model Audit Rule | Immutable audit trail via audit-service |
| SOX | Requirement → ADR → Code → Audit Event chain |
| BCBS-239 | Full data lineage from requirement to runtime |
| GDPR | Actor tracking in all audit events |
