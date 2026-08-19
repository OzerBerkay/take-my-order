package com.berkay.restaurant.service.domain.ports.output.repository;

import com.berkay.restaurant.service.domain.entity.Restaurant;
import com.berkay.restaurant.service.domain.valueobject.CuisineType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RestaurantRepository {



    Optional<Restaurant> findRestaurantById(UUID restaurantId);

    Restaurant saveRestaurant(Restaurant restaurant);
    
    List<Restaurant> findAllByIdIn(List<UUID> restaurantIds);
    
    com.berkay.restaurant.service.domain.dto.read.RestaurantPageResult findPublicRestaurants(String name, CuisineType cuisineType, Boolean available, int page, int size);
}
