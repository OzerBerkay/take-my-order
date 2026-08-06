CREATE TABLE IF NOT EXISTS identity.permission_outbox (
    id uuid NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    processed_at TIMESTAMP WITH TIME ZONE,
    type character varying COLLATE pg_catalog."default" NOT NULL,
    payload text COLLATE pg_catalog."default" NOT NULL,
    outbox_status character varying COLLATE pg_catalog."default" NOT NULL,
    version bigint NOT NULL,
    CONSTRAINT permission_outbox_pkey PRIMARY KEY (id)
);
