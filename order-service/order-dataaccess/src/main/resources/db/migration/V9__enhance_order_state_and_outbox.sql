-- V9__enhance_order_state_and_outbox.sql

-- 1. Add new lifecycle states to "order".order_status enum
ALTER TYPE "order".order_status ADD VALUE IF NOT EXISTS 'WAITING_FOR_APPROVAL';
ALTER TYPE "order".order_status ADD VALUE IF NOT EXISTS 'PREPARING';
ALTER TYPE "order".order_status ADD VALUE IF NOT EXISTS 'READY_FOR_PICKUP';
ALTER TYPE "order".order_status ADD VALUE IF NOT EXISTS 'IN_DELIVERY';
ALTER TYPE "order".order_status ADD VALUE IF NOT EXISTS 'DELIVERED';

-- 2. Enhance "order".orders table with optimistic locking version and lifecycle metadata
ALTER TABLE "order".orders ADD COLUMN IF NOT EXISTS version INTEGER NOT NULL DEFAULT 0;
ALTER TABLE "order".orders ADD COLUMN IF NOT EXISTS delivery_fee NUMERIC(10, 2) NOT NULL DEFAULT 0.00;
ALTER TABLE "order".orders ADD COLUMN IF NOT EXISTS cancel_reason VARCHAR(50);
ALTER TABLE "order".orders ADD COLUMN IF NOT EXISTS material_action VARCHAR(50);
ALTER TABLE "order".orders ADD COLUMN IF NOT EXISTS internal_note TEXT;

-- 3. Drop legacy failure_messages column from orders table
ALTER TABLE "order".orders DROP COLUMN IF EXISTS failure_messages;

-- 4. Fix order_address primary key to single id column to match JPA entity mapping
ALTER TABLE "order".order_address DROP CONSTRAINT IF EXISTS order_address_pkey;
ALTER TABLE "order".order_address ADD CONSTRAINT order_address_pkey PRIMARY KEY (id);

-- 5. Add product_name snapshot column to order_items and backfill from product_replica
ALTER TABLE "order".order_items ADD COLUMN IF NOT EXISTS product_name VARCHAR(255);

UPDATE "order".order_items oi
SET product_name = pr.name
FROM "order".product_replica pr
WHERE oi.product_id = pr.product_id AND oi.product_name IS NULL;

UPDATE "order".order_items SET product_name = 'Product' WHERE product_name IS NULL;

ALTER TABLE "order".order_items ALTER COLUMN product_name SET NOT NULL;

-- 6. Add operational performance indexes
CREATE INDEX IF NOT EXISTS idx_orders_restaurant_status ON "order".orders (restaurant_id, order_status);
CREATE INDEX IF NOT EXISTS idx_orders_tracking_id ON "order".orders (tracking_id);
