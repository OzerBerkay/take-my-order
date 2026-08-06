-- V6__create_organizational_units_replica.sql

CREATE TABLE organizational_units_replica (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    type VARCHAR(50) NOT NULL, -- MERCHANT, INTERNAL
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Index for searching by type
CREATE INDEX idx_org_units_replica_type ON organizational_units_replica(type);
