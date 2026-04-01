# ADR-0003: Audit Trail Aggregation with CloudEvents v1.0

## Status
Accepted

## Date
2025-01-15

## Context
The platform requires a comprehensive, immutable audit trail for all write operations. Insurance regulations mandate tracking of policyholder data changes, premium recalculations, and coverage evaluations. We need a standardized event format and an aggregation strategy.

## Decision
We will use **CloudEvents v1.0** as the event envelope format. Key design decisions:
1. All write operations emit a CloudEvent via HTTP POST to the audit-service
2. Events include structured `data` payloads with `entity_id`, `entity_type`, `action`, `actor`, and `changes` (before/after snapshots)
3. The audit-service stores both parsed fields and the raw event JSON for forensic analysis
4. The `/api/v2/audit-trail` endpoint provides enriched views by cross-referencing customer and policy data
5. Audit event emission is fire-and-forget — service availability does not block business operations

## Consequences
- **Positive**: Standards-compliant event format, interoperable with CNCF ecosystem
- **Positive**: Full before/after change tracking for regulatory compliance
- **Positive**: Enrichment provides human-readable audit views
- **Negative**: Cross-service enrichment adds latency to audit trail queries
- **Mitigation**: Enrichment failures are graceful — raw audit data is always available
