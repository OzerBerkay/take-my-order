-- Add new columns to restaurants
ALTER TABLE restaurant.restaurants
    ADD COLUMN IF NOT EXISTS district character varying COLLATE pg_catalog."default",
    ADD COLUMN IF NOT EXISTS neighborhood character varying COLLATE pg_catalog."default",
    ADD COLUMN IF NOT EXISTS building_number character varying COLLATE pg_catalog."default",
    ADD COLUMN IF NOT EXISTS door_number character varying COLLATE pg_catalog."default";

-- Drop postal_code from restaurants
ALTER TABLE restaurant.restaurants
    DROP COLUMN IF EXISTS postal_code;
