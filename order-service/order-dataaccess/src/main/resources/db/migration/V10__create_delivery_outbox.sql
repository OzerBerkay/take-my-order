CREATE TABLE "order".delivery_outbox (
    id uuid NOT NULL,
    order_id uuid NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    processed_at TIMESTAMP WITH TIME ZONE,
    type character varying(50) NOT NULL,
    payload text NOT NULL,
    outbox_status character varying(50) NOT NULL,
    version integer NOT NULL,
    CONSTRAINT delivery_outbox_pkey PRIMARY KEY (id)
);

CREATE INDEX idx_delivery_outbox_status ON "order".delivery_outbox (type, outbox_status);
CREATE INDEX idx_delivery_outbox_order_id ON "order".delivery_outbox (order_id);
