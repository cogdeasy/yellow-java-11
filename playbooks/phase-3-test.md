# Phase 3: Test

## Objective
Verify the implementation with comprehensive testing including integration tests.

## Steps

1. **Run Unit Tests**
   ```bash
   mvn test
   ```

2. **Write Integration Tests**
   - Use Testcontainers with real PostgreSQL
   - Test full request/response cycle
   - Verify database state changes
   - Test inter-service communication

3. **Run Integration Tests**
   ```bash
   mvn verify
   ```

4. **Manual Verification**
   ```bash
   docker-compose up -d
   # Test endpoints with curl
   curl http://localhost:3001/health
   curl http://localhost:3002/health
   curl http://localhost:3003/health
   ```

5. **Verify Audit Trail**
   - Create a customer
   - Change address to FL/CA/TX ZIP
   - Verify audit events in audit-service
   - Verify premium recalculation

6. **Integration Validation (HITL Gate 3)**
   - All tests green
   - Audit trail complete
   - Premium calculations correct

## Checklist
- [ ] All unit tests passing
- [ ] Integration tests with Testcontainers passing
- [ ] Manual endpoint verification
- [ ] Audit trail verification
- [ ] Gate 3 approval
