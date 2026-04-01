-- Yellow Insurance Claims Management System
-- Database Schema

CREATE TABLE IF NOT EXISTS customers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    email VARCHAR(255),
    phone VARCHAR(20),
    -- VULNERABILITY: SSN stored in plain text
    ssn VARCHAR(11),
    date_of_birth DATE,
    street VARCHAR(255),
    city VARCHAR(100),
    state VARCHAR(2),
    zip VARCHAR(10),
    -- VULNERABILITY: Password hash using weak algorithm
    password_hash VARCHAR(255),
    status VARCHAR(20) DEFAULT 'ACTIVE',
    risk_score INT DEFAULT 50,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS policies (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    policy_number VARCHAR(50) UNIQUE,
    customer_id BIGINT,
    policy_type VARCHAR(50),
    coverage_amount DECIMAL(15, 2),
    premium DECIMAL(10, 2),
    deductible DECIMAL(10, 2),
    start_date DATE,
    end_date DATE,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    risk_category VARCHAR(20),
    underwriter VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    -- ISSUE: No foreign key constraint to customers table
);

CREATE TABLE IF NOT EXISTS claims (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    claim_number VARCHAR(50) UNIQUE,
    policy_id BIGINT,
    customer_id BIGINT,
    claim_type VARCHAR(50),
    description VARCHAR(4000),
    status VARCHAR(20) DEFAULT 'OPEN',
    amount_claimed DECIMAL(15, 2),
    amount_approved DECIMAL(15, 2),
    incident_date TIMESTAMP,
    filed_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    resolved_date TIMESTAMP,
    adjuster_notes VARCHAR(4000),
    assigned_adjuster VARCHAR(100),
    document_path VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    -- ISSUE: No foreign key constraints
    -- ISSUE: No indexes on frequently queried columns
);

CREATE TABLE IF NOT EXISTS documents (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    claim_id BIGINT,
    customer_id BIGINT,
    file_name VARCHAR(255),
    file_path VARCHAR(500),
    content_type VARCHAR(100),
    file_size BIGINT,
    uploaded_by VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    -- ISSUE: No foreign key constraints
);

CREATE TABLE IF NOT EXISTS audit_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    entity_type VARCHAR(50),
    entity_id BIGINT,
    action VARCHAR(50),
    performed_by VARCHAR(100),
    old_value VARCHAR(10000),
    new_value VARCHAR(10000),
    ip_address VARCHAR(45),
    user_agent VARCHAR(1000),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    -- ISSUE: No indexes on entity_type + entity_id
    -- ISSUE: No partitioning strategy for large audit tables
);
