-- V19__enhance_restaurant_approval_outbox.sql

-- 1. Convert rigid native enum columns to flexible VARCHAR(50) (Bulgu 17 & Section 1.0.F.3 standard)
ALTER TABLE restaurant.order_approval ALTER COLUMN status TYPE VARCHAR(50);
ALTER TABLE restaurant.order_outbox ALTER COLUMN approval_status TYPE VARCHAR(50);
ALTER TABLE restaurant.order_outbox ALTER COLUMN outbox_status TYPE VARCHAR(50);

-- 2. Add version columns for optimistic locking (Lost Update prevention)
ALTER TABLE restaurant.restaurants ADD COLUMN IF NOT EXISTS version INTEGER NOT NULL DEFAULT 0;
ALTER TABLE restaurant.products ADD COLUMN IF NOT EXISTS version INTEGER NOT NULL DEFAULT 0;

-- 3. Drop legacy native enum types
DROP TYPE IF EXISTS restaurant.order_approval_status;
DROP TYPE IF EXISTS restaurant.approval_status;
DROP TYPE IF EXISTS restaurant.outbox_status;
