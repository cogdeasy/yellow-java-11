# TRiSM Policy — Trust, Risk, and Security Management

## Overview
This document defines the Trust, Risk, and Security Management (TRiSM) policy for the Liberty Mutual Insurance Platform. TRiSM ensures that AI-assisted development maintains transparency, accountability, and security.

## Principles

### 1. Transparency
- All AI-generated code changes MUST be clearly attributed in commit messages
- ADR documents MUST be referenced in PRs that modify core business logic
- Audit trail provides full traceability of all data changes

### 2. Risk Management
- Premium recalculation uses deterministic, auditable formulas (ADR-0004)
- Unknown ZIP codes trigger manual review rather than silent defaults
- States requiring mandatory coverage re-evaluation (FL, CA, TX) are explicitly coded
- Kill switch enables immediate service degradation if anomalies detected

### 3. Security
- No hardcoded secrets — all credentials via environment variables
- Input validation on all API endpoints (JSR 380)
- SQL injection prevention via parameterized queries (Spring Data JPA)
- CORS and rate limiting configured at infrastructure level

## Compliance Controls

### Data Integrity
- All write operations emit CloudEvents v1.0 audit events
- Before/after snapshots capture exact field changes
- Raw event JSON preserved for forensic analysis

### Access Control
- Service-to-service communication via internal network
- External API access requires authentication (future: OAuth 2.0)
- Database credentials rotated via environment variables

### Monitoring
- Health check endpoints on all services (`/health`)
- Spring Boot Actuator for operational metrics
- Structured logging with correlation IDs

## Review Cadence
- Monthly: Review audit trail for anomalies
- Quarterly: Update risk assessments for ZIP risk data
- Annually: Full TRiSM policy review
