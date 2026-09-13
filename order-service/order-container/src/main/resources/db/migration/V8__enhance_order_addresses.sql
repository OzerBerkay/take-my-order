-- Add new columns to order_address
ALTER TABLE "order".order_address
    ADD COLUMN IF NOT EXISTS district character varying COLLATE pg_catalog."default",
    ADD COLUMN IF NOT EXISTS neighborhood character varying COLLATE pg_catalog."default",
    ADD COLUMN IF NOT EXISTS building_number character varying COLLATE pg_catalog."default",
    ADD COLUMN IF NOT EXISTS door_number character varying COLLATE pg_catalog."default",
    ADD COLUMN IF NOT EXISTS floor integer,
    ADD COLUMN IF NOT EXISTS address_instructions character varying COLLATE pg_catalog."default",
    ADD COLUMN IF NOT EXISTS contact_first_name character varying COLLATE pg_catalog."default",
    ADD COLUMN IF NOT EXISTS contact_last_name character varying COLLATE pg_catalog."default",
    ADD COLUMN IF NOT EXISTS contact_phone character varying COLLATE pg_catalog."default";

-- Drop postal_code from order_address
ALTER TABLE "order".order_address
    DROP COLUMN IF EXISTS postal_code;
