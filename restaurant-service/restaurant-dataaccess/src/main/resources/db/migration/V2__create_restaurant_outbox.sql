DROP TABLE IF EXISTS restaurant.restaurant_outbox CASCADE;

CREATE TABLE restaurant.restaurant_outbox
(
    id uuid NOT NULL,
    saga_id uuid NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    processed_at TIMESTAMP WITH TIME ZONE,
    type character varying COLLATE pg_catalog."default" NOT NULL,
    payload jsonb NOT NULL,
    outbox_status character varying COLLATE pg_catalog."default" NOT NULL,
    version integer NOT NULL,
    CONSTRAINT restaurant_outbox_pkey PRIMARY KEY (id)
);

CREATE INDEX "restaurant_outbox_saga_status"
    ON restaurant.restaurant_outbox
        (type, outbox_status, saga_id);