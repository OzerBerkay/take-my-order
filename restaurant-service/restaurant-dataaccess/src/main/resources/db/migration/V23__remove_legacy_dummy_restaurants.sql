-- V23__remove_legacy_dummy_restaurants.sql
-- Remove legacy hardcoded dummy restaurants (Burger King & McDonalds) from V1__init.sql and any related records

DELETE FROM restaurant.order_approval 
WHERE restaurant_id IN (
    'd215b5f8-0249-4dc5-89a3-51fd148cfb45',
    'd215b5f8-0249-4dc5-89a3-51fd148cfb46'
);

DELETE FROM restaurant.products 
WHERE restaurant_id IN (
    'd215b5f8-0249-4dc5-89a3-51fd148cfb45',
    'd215b5f8-0249-4dc5-89a3-51fd148cfb46'
);

DELETE FROM restaurant.restaurants 
WHERE restaurant_id IN (
    'd215b5f8-0249-4dc5-89a3-51fd148cfb45',
    'd215b5f8-0249-4dc5-89a3-51fd148cfb46'
);

