# Playbook: Requirement Synthesis (BA Agent) — Java 11 Edition

## Phase 1 of the Agentic Workflow Protocol

### Purpose
Analyze stakeholder requirements and generate structured Gherkin scenarios
for human approval before any design or implementation work begins.

### Prerequisites
- PRD or requirement document provided
- Access to the codebase for DDD boundary analysis
- DeepWiki indexed for existing service context

### Steps

#### Step 1: Ingest Requirements
```
1. Read the PRD/requirement document
2. Identify the key user stories and acceptance criteria
3. Map requirements to existing service boundaries (DDD)
   - customer-service (Spring Boot, port 8081): Policyholder Records
   - policy-service (Spring Boot, port 8082): Policy Platform
   - audit-service (Spring Boot, port 8083): Compliance Data Store
4. Query DeepWiki for existing architecture context
5. Identify insurance domain constraints (state regulations, compliance)
```

#### Step 2: Generate Gherkin Scenarios
```
1. Create a new .feature file in docs/specs/requirements/
2. Write Feature description with As a/I want/So that
3. Write Background section with service dependencies
4. Write Scenarios for each acceptance criterion
5. Tag scenarios: @phase-1-approved (pending), @happy-path, @error, @compliance
6. Include Requirement_ID in comments
7. For insurance domain: include @coverage-check and @premium-calc tags as applicable
```

#### Step 3: Create Linear/Jira Tickets
```
1. Create a ticket for each scenario group
2. Include: Feature name, Gherkin scenarios, acceptance criteria
3. Link to requirement source document
4. Set status to "Pending PO Approval"
```

#### Step 4: HITL Gate 1 - Requirement Sign-off
```
1. Open a PR with the .feature file
2. Tag the Product Owner for review
3. WAIT: Do not proceed to Phase 2 until PO approves
4. No API credits spent on design/code until this gate passes
```

### Java 11 Specific Notes
- Gherkin scenarios will be executed by **Cucumber-JVM 7.x** with JUnit 5
- Step definitions live in `services/{service}/src/test/java/.../bdd/steps/`
- Feature files are copied to `services/{service}/src/test/resources/features/` during Phase 3
- Integration tests use **Testcontainers** with real PostgreSQL (no H2, no mocking)

### Constraints
- Cannot trigger Architect Agent until human PO approves scenarios
- All requirements tagged with Requirement_ID for TRiSM traceability
- Feature files follow standard Gherkin syntax
- Insurance-specific scenarios must tag compliance requirements (NAIC, SOX)

### Output Artifacts
- `docs/specs/requirements/{feature-name}.feature`
- Linear/Jira tickets with acceptance criteria
- PR for human review

### TRiSM Link
Every approved requirement is tagged with a Requirement_ID used in all
downstream logs, ADRs, and audit events.
