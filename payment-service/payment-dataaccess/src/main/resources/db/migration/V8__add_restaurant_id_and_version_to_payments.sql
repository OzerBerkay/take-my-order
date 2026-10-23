-- V8__add_restaurant_id_and_version_to_payments.sql

-- 1. Add restaurant_id and version to "payment".payments table
ALTER TABLE "payment".payments ADD COLUMN IF NOT EXISTS restaurant_id uuid;
ALTER TABLE "payment".payments ADD COLUMN IF NOT EXISTS version integer NOT NULL DEFAULT 0;

-- 2. Backfill existing records if any
UPDATE "payment".payments SET restaurant_id = '00000000-0000-0000-0000-000000000000' WHERE restaurant_id IS NULL;
ALTER TABLE "payment".payments ALTER COLUMN restaurant_id SET NOT NULL;

-- 3. Create index on restaurant_id
CREATE INDEX IF NOT EXISTS idx_payments_restaurant_id ON "payment".payments (restaurant_id);
