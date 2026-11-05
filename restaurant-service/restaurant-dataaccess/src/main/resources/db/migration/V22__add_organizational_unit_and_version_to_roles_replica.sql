-- V22__add_organizational_unit_and_version_to_roles_replica.sql

-- 1. Ensure outbox payload column is TEXT to match Hibernate entity mapping
ALTER TABLE restaurant.order_outbox ALTER COLUMN payload TYPE TEXT;
ALTER TABLE restaurant.restaurant_outbox ALTER COLUMN payload TYPE TEXT;

-- 2. Add missing columns to roles_replica
ALTER TABLE restaurant.roles_replica ADD COLUMN IF NOT EXISTS organizational_unit_id UUID;
ALTER TABLE restaurant.roles_replica ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_schema = 'restaurant' AND table_name = 'roles_replica' AND column_name = 'context_id'
    ) THEN
        UPDATE restaurant.roles_replica 
        SET organizational_unit_id = context_id 
        WHERE organizational_unit_id IS NULL AND context_id IS NOT NULL;
    END IF;
END $$;
