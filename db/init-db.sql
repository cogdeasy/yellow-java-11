-- Liberty Mutual Insurance Platform — Database Schema
-- ADR-0001: Monorepo structure with shared PostgreSQL
-- All three services share a single database with separate tables

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ============================================================
-- CUSTOMERS TABLE
-- ============================================================
CREATE TABLE IF NOT EXISTS customers (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    phone VARCHAR(20),
    street VARCHAR(255) NOT NULL,
    city VARCHAR(100) NOT NULL,
    state CHAR(2) NOT NULL,
    zip CHAR(5) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'active',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_customers_status ON customers(status);
CREATE INDEX IF NOT EXISTS idx_customers_state ON customers(state);
CREATE INDEX IF NOT EXISTS idx_customers_city ON customers(city);
CREATE INDEX IF NOT EXISTS idx_customers_email ON customers(email);

-- ============================================================
-- POLICIES TABLE
-- ============================================================
CREATE TABLE IF NOT EXISTS policies (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    customer_id UUID NOT NULL REFERENCES customers(id),
    type VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'active',
    premium_annual DECIMAL(10, 2) NOT NULL,
    coverage_amount DECIMAL(12, 2) NOT NULL,
    zip_code CHAR(5),
    effective_date DATE NOT NULL,
    expiry_date DATE NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_policies_customer_id ON policies(customer_id);
CREATE INDEX IF NOT EXISTS idx_policies_type ON policies(type);
CREATE INDEX IF NOT EXISTS idx_policies_status ON policies(status);

-- ============================================================
-- AUDIT EVENTS TABLE (CloudEvents v1.0)
-- ============================================================
CREATE TABLE IF NOT EXISTS audit_events (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    specversion VARCHAR(10) NOT NULL DEFAULT '1.0',
    event_type VARCHAR(100) NOT NULL,
    source VARCHAR(100) NOT NULL,
    event_time TIMESTAMPTZ NOT NULL,
    entity_id UUID,
    entity_type VARCHAR(50),
    action VARCHAR(100),
    actor VARCHAR(100),
    changes JSONB DEFAULT '{}',
    raw_event JSONB NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_audit_events_entity_type ON audit_events(entity_type);
CREATE INDEX IF NOT EXISTS idx_audit_events_entity_id ON audit_events(entity_id);
CREATE INDEX IF NOT EXISTS idx_audit_events_event_type ON audit_events(event_type);
CREATE INDEX IF NOT EXISTS idx_audit_events_source ON audit_events(source);
CREATE INDEX IF NOT EXISTS idx_audit_events_event_time ON audit_events(event_time);

-- ============================================================
-- SEED DATA
-- ============================================================

-- Sample customers
INSERT INTO customers (id, first_name, last_name, email, phone, street, city, state, zip) VALUES
    ('a1b2c3d4-e5f6-4a7b-8c9d-0e1f2a3b4c5d', 'John', 'Doe', 'john.doe@example.com', '555-0100', '123 Main St', 'Boston', 'MA', '02101'),
    ('b2c3d4e5-f6a7-4b8c-9d0e-1f2a3b4c5d6e', 'Jane', 'Smith', 'jane.smith@example.com', '555-0200', '456 Oak Ave', 'Miami', 'FL', '33101'),
    ('c3d4e5f6-a7b8-4c9d-0e1f-2a3b4c5d6e7f', 'Robert', 'Johnson', 'robert.j@example.com', '555-0300', '789 Pine Rd', 'Los Angeles', 'CA', '90001'),
    ('d4e5f6a7-b8c9-4d0e-1f2a-3b4c5d6e7f8a', 'Maria', 'Garcia', 'maria.g@example.com', '555-0400', '321 Elm St', 'Houston', 'TX', '77001'),
    ('e5f6a7b8-c9d0-4e1f-2a3b-4c5d6e7f8a9b', 'David', 'Wilson', 'david.w@example.com', '555-0500', '654 Maple Dr', 'New York', 'NY', '10001')
ON CONFLICT (email) DO NOTHING;

-- Sample policies
INSERT INTO policies (id, customer_id, type, status, premium_annual, coverage_amount, zip_code, effective_date, expiry_date) VALUES
    ('f6a7b8c9-d0e1-4f2a-3b4c-5d6e7f8a9b0c', 'a1b2c3d4-e5f6-4a7b-8c9d-0e1f2a3b4c5d', 'home', 'active', 5500.00, 500000.00, '02101', '2025-01-01', '2026-01-01'),
    ('a7b8c9d0-e1f2-4a3b-4c5d-6e7f8a9b0c1d', 'a1b2c3d4-e5f6-4a7b-8c9d-0e1f2a3b4c5d', 'auto', 'active', 2200.00, 200000.00, '02101', '2025-01-01', '2026-01-01'),
    ('b8c9d0e1-f2a3-4b4c-5d6e-7f8a9b0c1d2e', 'b2c3d4e5-f6a7-4b8c-9d0e-1f2a3b4c5d6e', 'home', 'active', 8750.00, 500000.00, '33101', '2025-03-01', '2026-03-01'),
    ('c9d0e1f2-a3b4-4c5d-6e7f-8a9b0c1d2e3f', 'c3d4e5f6-a7b8-4c9d-0e1f-2a3b4c5d6e7f', 'home', 'active', 7750.00, 500000.00, '90001', '2025-06-01', '2026-06-01'),
    ('d0e1f2a3-b4c5-4d6e-7f8a-9b0c1d2e3f4a', 'd4e5f6a7-b8c9-4d0e-1f2a-3b4c5d6e7f8a', 'home', 'active', 8000.00, 500000.00, '77001', '2025-02-01', '2026-02-01'),
    ('e1f2a3b4-c5d6-4e7f-8a9b-0c1d2e3f4a5b', 'e5f6a7b8-c9d0-4e1f-2a3b-4c5d6e7f8a9b', 'auto', 'active', 2600.00, 200000.00, '10001', '2025-04-01', '2026-04-01')
ON CONFLICT DO NOTHING;
