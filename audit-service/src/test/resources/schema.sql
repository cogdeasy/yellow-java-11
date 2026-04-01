CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

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
