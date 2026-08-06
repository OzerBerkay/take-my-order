ALTER TABLE "restaurant".restaurants
    ADD COLUMN street character varying COLLATE pg_catalog."default" NOT NULL DEFAULT '',
    ADD COLUMN city character varying COLLATE pg_catalog."default" NOT NULL DEFAULT '',
    ADD COLUMN postal_code character varying COLLATE pg_catalog."default" NOT NULL DEFAULT '',
    ADD COLUMN phone_number character varying COLLATE pg_catalog."default" NOT NULL DEFAULT '',
    ADD COLUMN minimum_order_amount numeric(10,2),
    ADD COLUMN delivery_fee numeric(10,2),
    ADD COLUMN average_delivery_time_in_minutes integer,
    ADD COLUMN cuisine_type character varying COLLATE pg_catalog."default" NOT NULL DEFAULT 'OTHER',
    ADD COLUMN description text,
    ADD COLUMN logo_url character varying COLLATE pg_catalog."default";
