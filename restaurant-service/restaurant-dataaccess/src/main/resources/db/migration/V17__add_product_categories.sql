CREATE TABLE restaurant.product_categories (
    id UUID PRIMARY KEY,
    restaurant_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    sort_order INTEGER NOT NULL,
    CONSTRAINT fk_product_categories_restaurant FOREIGN KEY (restaurant_id) REFERENCES restaurant.restaurants(restaurant_id) ON DELETE CASCADE
);

-- Ensure we don't break NOT NULL constraint for existing records
DELETE FROM restaurant.products;

ALTER TABLE restaurant.products ADD COLUMN category_id UUID NOT NULL;

ALTER TABLE restaurant.products ADD CONSTRAINT fk_products_category FOREIGN KEY (category_id) REFERENCES restaurant.product_categories(id) ON DELETE RESTRICT;
