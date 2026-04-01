-- Seed data for Yellow Insurance Claims Management System
-- VULNERABILITY: Contains realistic-looking PII data including SSNs

INSERT INTO customers (first_name, last_name, email, phone, ssn, date_of_birth, street, city, state, zip, password_hash, status, risk_score) VALUES
('John', 'Smith', 'john.smith@email.com', '555-0101', '123-45-6789', '1985-03-15', '123 Oak St', 'Miami', 'FL', '33101', '5f4dcc3b5aa765d61d8327deb882cf99', 'ACTIVE', 65),
('Jane', 'Doe', 'jane.doe@email.com', '555-0102', '987-65-4321', '1990-07-22', '456 Pine Ave', 'Beverly Hills', 'CA', '90210', '5f4dcc3b5aa765d61d8327deb882cf99', 'ACTIVE', 62),
('Bob', 'Johnson', 'bob.johnson@email.com', '555-0103', '456-78-9012', '1978-11-30', '789 Elm Blvd', 'Houston', 'TX', '77001', '5f4dcc3b5aa765d61d8327deb882cf99', 'ACTIVE', 60),
('Alice', 'Williams', 'alice.w@email.com', '555-0104', '234-56-7890', '1992-05-18', '321 Maple Dr', 'New York', 'NY', '10001', '5f4dcc3b5aa765d61d8327deb882cf99', 'ACTIVE', 55),
('Charlie', 'Brown', 'charlie.b@email.com', '555-0105', '345-67-8901', '1988-09-10', '654 Birch Ln', 'Denver', 'CO', '80201', '5f4dcc3b5aa765d61d8327deb882cf99', 'ACTIVE', 53),
('Diana', 'Prince', 'diana.p@email.com', '555-0106', '567-89-0123', '1995-01-25', '987 Cedar Ct', 'Boston', 'MA', '02101', '5f4dcc3b5aa765d61d8327deb882cf99', 'INACTIVE', 58),
('Edward', 'Jones', 'ed.jones@email.com', '555-0107', '678-90-1234', '1975-12-05', '147 Walnut St', 'New Orleans', 'LA', '70112', '5f4dcc3b5aa765d61d8327deb882cf99', 'ACTIVE', 63),
('Fiona', 'Garcia', 'fiona.g@email.com', '555-0108', '789-01-2345', '1983-06-14', '258 Spruce Way', 'Phoenix', 'AZ', '85001', '5f4dcc3b5aa765d61d8327deb882cf99', 'ACTIVE', 57);

INSERT INTO policies (policy_number, customer_id, policy_type, coverage_amount, premium, deductible, start_date, end_date, status, risk_category, underwriter) VALUES
('HOME-2024-0001', 1, 'HOME', 350000.00, 2450.00, 2500.00, '2024-01-01', '2025-01-01', 'ACTIVE', 'HIGH', 'Susan Miller'),
('AUTO-2024-0002', 1, 'AUTO', 50000.00, 1200.00, 1000.00, '2024-01-01', '2025-01-01', 'ACTIVE', 'MEDIUM', 'Susan Miller'),
('HOME-2024-0003', 2, 'HOME', 750000.00, 3800.00, 5000.00, '2024-02-01', '2025-02-01', 'ACTIVE', 'HIGH', 'Mark Davis'),
('AUTO-2024-0004', 3, 'AUTO', 35000.00, 950.00, 500.00, '2024-03-01', '2025-03-01', 'ACTIVE', 'MEDIUM', 'Susan Miller'),
('HOME-2024-0005', 3, 'HOME', 280000.00, 2100.00, 2000.00, '2024-03-01', '2025-03-01', 'ACTIVE', 'HIGH', 'Mark Davis'),
('LIFE-2024-0006', 4, 'LIFE', 500000.00, 450.00, 0.00, '2024-01-15', '2034-01-15', 'ACTIVE', 'LOW', 'Amy Chen'),
('HOME-2024-0007', 5, 'HOME', 320000.00, 1800.00, 1500.00, '2024-04-01', '2025-04-01', 'ACTIVE', 'LOW', 'Mark Davis'),
('HLTH-2024-0008', 6, 'HEALTH', 100000.00, 3200.00, 1500.00, '2024-01-01', '2025-01-01', 'EXPIRED', 'MEDIUM', 'Amy Chen'),
('HOME-2024-0009', 7, 'HOME', 220000.00, 2800.00, 2000.00, '2024-05-01', '2025-05-01', 'ACTIVE', 'HIGH', 'Susan Miller'),
('AUTO-2024-0010', 8, 'AUTO', 45000.00, 1100.00, 750.00, '2024-06-01', '2025-06-01', 'ACTIVE', 'MEDIUM', 'Mark Davis');

INSERT INTO claims (claim_number, policy_id, customer_id, claim_type, description, status, amount_claimed, amount_approved, incident_date, filed_date, adjuster_notes, assigned_adjuster) VALUES
('CLM-2024-0001', 1, 1, 'WATER_DAMAGE', 'Pipe burst in basement causing flooding to finished area', 'APPROVED', 15000.00, 12500.00, '2024-06-15 10:30:00', '2024-06-16 09:00:00', 'Verified damage. Deductible applied.', 'Tom Wilson'),
('CLM-2024-0002', 3, 2, 'WILDFIRE', 'Brush fire damaged exterior siding and landscaping', 'OPEN', 45000.00, NULL, '2024-07-20 14:00:00', '2024-07-21 11:00:00', 'Pending inspection', 'Lisa Anderson'),
('CLM-2024-0003', 2, 1, 'COLLISION', 'Rear-end collision at intersection. Body damage to bumper and trunk.', 'APPROVED', 8500.00, 7500.00, '2024-08-05 17:30:00', '2024-08-06 10:00:00', 'At-fault accident confirmed by police report', 'Tom Wilson'),
('CLM-2024-0004', 5, 3, 'WIND_DAMAGE', 'Hurricane damage to roof shingles and gutters', 'UNDER_REVIEW', 22000.00, NULL, '2024-09-10 08:00:00', '2024-09-12 14:00:00', 'Waiting for contractor estimate', 'Lisa Anderson'),
('CLM-2024-0005', 9, 7, 'FLOOD', 'Flooding from hurricane caused first floor water damage', 'OPEN', 55000.00, NULL, '2024-09-10 06:00:00', '2024-09-15 09:00:00', 'Major claim - needs senior adjuster review', 'Tom Wilson'),
('CLM-2024-0006', 7, 5, 'THEFT', 'Break-in. Electronics and jewelry stolen from home.', 'DENIED', 12000.00, 0.00, '2024-10-01 23:00:00', '2024-10-03 08:00:00', 'Insufficient evidence of break-in. No police report filed within 48 hours.', 'Lisa Anderson'),
('CLM-2024-0007', 4, 3, 'COLLISION', 'Single vehicle accident. Hydroplaning on wet road.', 'AUTO_APPROVED', 3500.00, 3500.00, '2024-10-15 06:30:00', '2024-10-15 12:00:00', 'Auto-approved: under $5000 threshold', 'system-auto'),
('CLM-2024-0008', 1, 1, 'MOLD', 'Mold discovered in walls following water damage claim CLM-2024-0001', 'OPEN', 18000.00, NULL, '2024-11-01 09:00:00', '2024-11-02 10:00:00', 'Related to previous claim. Needs specialist assessment.', 'Tom Wilson');

INSERT INTO audit_logs (entity_type, entity_id, action, performed_by, old_value, new_value, created_at) VALUES
('CLAIM', 1, 'CREATED', 'system', NULL, 'Claim CLM-2024-0001 created', '2024-06-16 09:00:00'),
('CLAIM', 1, 'STATUS_CHANGED', 'Tom Wilson', 'OPEN', 'APPROVED', '2024-06-20 15:00:00'),
('CLAIM', 3, 'CREATED', 'system', NULL, 'Claim CLM-2024-0003 created', '2024-08-06 10:00:00'),
('CLAIM', 3, 'STATUS_CHANGED', 'Tom Wilson', 'OPEN', 'APPROVED', '2024-08-10 16:00:00'),
('CUSTOMER', 1, 'ADDRESS_CHANGED', 'system', '100 Old St, Tampa, FL 33601', '123 Oak St, Miami, FL 33101', '2024-05-01 09:00:00'),
('CLAIM', 7, 'CREATED', 'system', NULL, 'Claim CLM-2024-0007 created', '2024-10-15 12:00:00'),
('CLAIM', 7, 'STATUS_CHANGED', 'system-auto', 'OPEN', 'AUTO_APPROVED', '2024-10-15 12:00:01');
