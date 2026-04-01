# Phase 4: Review

## Objective
Get code review approval and compliance sign-off before deployment.

## Steps

1. **Create Pull Request**
   - Descriptive title and description
   - Link to ADR documents
   - Include test results summary

2. **Code Review (HITL Gate 2)**
   - At least 1 peer reviewer
   - Address all feedback
   - Verify CI passes

3. **Compliance Check (HITL Gate 4)**
   - All write operations emit audit events
   - Coverage re-evaluation triggers for FL/CA/TX
   - No hardcoded secrets
   - Input validation on all endpoints

4. **Address Feedback**
   - Fix issues identified in review
   - Push updates to PR branch
   - Re-request review if needed

## Checklist
- [ ] PR created with ADR links
- [ ] CI passing
- [ ] Code review approved (Gate 2)
- [ ] Compliance check passed (Gate 4)
