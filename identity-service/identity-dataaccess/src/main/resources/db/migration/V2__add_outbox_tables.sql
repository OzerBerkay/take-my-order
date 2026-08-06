-- V2__add_outbox_tables.sql

-- Drop the generic outbox table since we need specific ones
DROP TABLE IF EXISTS outbox;


-- ROLE_OUTBOX TABLE
CREATE TABLE role_outbox (
    id UUID PRIMARY KEY,
    type VARCHAR(100) NOT NULL,
    payload TEXT NOT NULL,
    outbox_status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    processed_at TIMESTAMP WITH TIME ZONE,
    version BIGINT NOT NULL DEFAULT 0
);
