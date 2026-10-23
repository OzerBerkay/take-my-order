CREATE TABLE payment.order_escrows (
    id uuid NOT NULL,
    order_id uuid NOT NULL,
    restaurant_id uuid NOT NULL,
    customer_id uuid NOT NULL,
    amount numeric(10,2) NOT NULL,
    status character varying(50) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    version integer NOT NULL,
    CONSTRAINT order_escrows_pkey PRIMARY KEY (id),
    CONSTRAINT order_escrows_order_id_key UNIQUE (order_id)
);

CREATE INDEX idx_order_escrows_restaurant_status ON payment.order_escrows (restaurant_id, status);
CREATE INDEX idx_order_escrows_created_at ON payment.order_escrows (created_at);
