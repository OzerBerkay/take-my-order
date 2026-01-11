-- 1. Restaurants Tablosu
-- Java Entity: RestaurantEntity (restaurantId, restaurantActive)
CREATE TABLE "order".restaurants
(
    restaurant_id uuid NOT NULL,
    restaurant_active boolean NOT NULL,
    CONSTRAINT restaurants_pkey PRIMARY KEY (restaurant_id)
);

-- 2. Products Tablosu
-- Java Entity: ProductEntity (productId, name, price, available, restaurant)
CREATE TABLE "order".products
(
    product_id uuid NOT NULL,
    name character varying COLLATE pg_catalog."default" NOT NULL,
    price numeric(10,2) NOT NULL,
    available boolean NOT NULL,
    restaurant_id uuid NOT NULL, -- ManyToOne ilişkisi için FK kolonu
    CONSTRAINT products_pkey PRIMARY KEY (product_id)
);

-- 3. İlişkiler (Foreign Key)
-- Product tablosundaki 'restaurant_id', Restaurants tablosundaki 'restaurant_id'ye gider.
-- On Delete Cascade: Restoran silinirse ürünleri de silinsin.
ALTER TABLE "order".products
    ADD CONSTRAINT "FK_PRODUCT_RESTAURANT" FOREIGN KEY (restaurant_id)
        REFERENCES "order".restaurants (restaurant_id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE CASCADE;