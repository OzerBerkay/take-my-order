-- V14__add_idempotency_key_to_orders.sql

-- 1. Add idempotency_key column to orders table if it doesn't exist, with fallback default
ALTER TABLE "order".orders ADD COLUMN IF NOT EXISTS idempotency_key character varying(255) DEFAULT ('legacy-' || gen_random_uuid()::text);

-- 2. Backfill existing legacy orders with deterministic unique keys
UPDATE "order".orders 
SET idempotency_key = 'legacy-' || id::text 
WHERE idempotency_key IS NULL;

-- 3. Enforce NOT NULL constraint
ALTER TABLE "order".orders ALTER COLUMN idempotency_key SET NOT NULL;

-- 4. Add UNIQUE constraint and index
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint 
        WHERE conname = 'orders_idempotency_key_key'
    ) THEN
        ALTER TABLE "order".orders ADD CONSTRAINT orders_idempotency_key_key UNIQUE (idempotency_key);
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_orders_idempotency_key ON "order".orders (idempotency_key);
