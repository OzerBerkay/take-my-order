CREATE TABLE "restaurant".restaurant_personnel
(
    id UUID NOT NULL,
    restaurant_id UUID NOT NULL,
    user_id UUID NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT restaurant_personnel_pkey PRIMARY KEY (id)
);

ALTER TABLE "restaurant".restaurant_personnel
    ADD CONSTRAINT "FK_RESTAURANT_PERSONNEL_ON_RESTAURANT" FOREIGN KEY (restaurant_id) REFERENCES "restaurant".restaurants (restaurant_id) MATCH SIMPLE
    ON UPDATE NO ACTION
    ON DELETE CASCADE
    NOT VALID;

CREATE UNIQUE INDEX "uq_restaurant_personnel" ON "restaurant".restaurant_personnel(restaurant_id, user_id);
