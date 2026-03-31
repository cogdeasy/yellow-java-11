# Human-in-the-Loop (HITL) Gates

## Overview
HITL gates are mandatory checkpoints where human approval is required before
an agent can proceed to the next phase. No API credits are spent on downstream
work until the gate passes.

## Gate Definitions

### Gate 1: Requirement Sign-off
- **Phase**: 1 → 2 (Requirement Synthesis → Architecture Review)
- **Approver**: Product Owner
- **Artifact**: Gherkin `.feature` file in `docs/specs/requirements/`
- **Mechanism**: GitHub PR review — PO approves or requests changes
- **Enforcement**: CI pipeline blocks Phase 2 playbook triggers until PR is merged

### Gate 2: Architecture Approval
- **Phase**: 2 → 3 (Architecture Review → Test-Forward Development)
- **Approver**: Lead Architect
- **Artifact**: ADR document in `docs/adr/`
- **Mechanism**: GitHub PR review — Architect approves or requests changes
- **Enforcement**: CI pipeline validates ADR format and blocks implementation PRs without linked ADR

## Enforcement
- CI pipelines check for gate compliance on every PR
- Agents must include gate status in PR descriptions
- Kill switch (`governance/kill-switch.yml`) can revert all agents to assistive mode
