# ADR-0004: Premium Recalculation via ZIP Risk API

## Status
Accepted

## Date
2025-01-15

## Context
When a policyholder changes their address, premiums must be recalculated based on the risk profile of the new location. Different ZIP codes carry different risk factors (flood zones, hurricane risk, wildfire risk, crime rates). Some states (FL, CA, TX) require mandatory coverage re-evaluation.

## Decision
We will implement a **ZipRisk API** within the policy-service that provides location-based risk factors:
1. `GET /api/v1/zip-risk/:zipcode` returns risk data including flood zone, hurricane risk, wildfire risk, crime rate, and a composite base modifier
2. Premium calculation: `coverage_amount × base_rate (0.01) × base_modifier`
3. Address changes trigger `POST /api/v1/policies/:id/recalculate` for each active policy
4. States in `STATES_REQUIRING_REVIEW` (FL, CA, TX) trigger mandatory coverage re-evaluation
5. Unknown ZIP codes flag the policy for manual review (`pending_review` status)

## ZIP Risk Data
| ZIP   | State | Flood | Hurricane | Wildfire | Modifier | Review |
|-------|-------|-------|-----------|----------|----------|--------|
| 02101 | MA    | No    | 0.20      | 0.05     | 1.10     | No     |
| 33101 | FL    | Yes   | 0.85      | 0.10     | 1.75     | Yes    |
| 90001 | CA    | No    | 0.00      | 0.70     | 1.55     | Yes    |
| 77001 | TX    | Yes   | 0.60      | 0.15     | 1.60     | Yes    |
| 10001 | NY    | No    | 0.15      | 0.00     | 1.30     | No     |
| 80201 | CO    | No    | 0.00      | 0.35     | 1.05     | No     |

## Consequences
- **Positive**: Deterministic, auditable premium calculations
- **Positive**: Clear trigger for coverage re-evaluation in high-risk states
- **Negative**: In-memory ZIP data limits scalability (production would use a database)
- **Mitigation**: Service is designed for easy swap to database-backed implementation
