# Phase 1: Design

## Objective
Define the feature scope, create ADR documents, and get design approval.

## Steps

1. **Identify the Change**
   - What feature or fix is being implemented?
   - Which services are affected?
   - What is the business impact?

2. **Create ADR Document**
   - Use the template in `docs/adr/`
   - Include: Context, Decision, Consequences
   - Reference related ADRs

3. **Define API Contract**
   - Update `docs/specs/api/openapi.yml`
   - Define request/response schemas
   - Document error responses

4. **Write Gherkin Specs**
   - Create feature files in `docs/specs/requirements/`
   - Cover happy path and error scenarios
   - Include boundary conditions

5. **Design Review (HITL Gate 1)**
   - Submit ADR for tech lead review
   - Address feedback
   - Get approval before proceeding to Phase 2

## Artifacts
- [ ] ADR document
- [ ] OpenAPI spec update
- [ ] Gherkin feature file(s)
- [ ] Gate 1 approval
