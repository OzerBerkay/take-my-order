-- V12__create_cuisines_table.sql

CREATE TABLE "restaurant".cuisines
(
    id uuid NOT NULL,
    name character varying COLLATE pg_catalog."default" NOT NULL,
    code character varying COLLATE pg_catalog."default" NOT NULL,
    description character varying COLLATE pg_catalog."default" NOT NULL,
    icon_url character varying COLLATE pg_catalog."default" NOT NULL,
    is_active boolean NOT NULL,
    CONSTRAINT cuisines_pkey PRIMARY KEY (id),
    CONSTRAINT cuisines_code_key UNIQUE (code)
);

CREATE TABLE "restaurant".restaurant_cuisines
(
    restaurant_id uuid NOT NULL,
    cuisine_id uuid NOT NULL,
    CONSTRAINT restaurant_cuisines_pkey PRIMARY KEY (restaurant_id, cuisine_id),
    CONSTRAINT fk_restaurant_cuisines_on_restaurant FOREIGN KEY (restaurant_id)
        REFERENCES "restaurant".restaurants (restaurant_id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE CASCADE,
    CONSTRAINT fk_restaurant_cuisines_on_cuisine FOREIGN KEY (cuisine_id)
        REFERENCES "restaurant".cuisines (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE CASCADE
);

ALTER TABLE "restaurant".restaurants DROP COLUMN cuisine_type;
