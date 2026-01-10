package com.berkay.restaurant.service.domain.ports.output.repository;

import com.berkay.restaurant.service.domain.entity.Restaurant;

import java.util.Optional;
import java.util.UUID;

public interface RestaurantRepository {

    Optional<Restaurant> findRestaurantById(UUID restaurantId);

    Restaurant saveRestaurant(Restaurant restaurant);
}
