package com.berkay.restaurant.service.domain.ports.output.repository;

import com.berkay.restaurant.service.domain.entity.Restaurant;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RestaurantRepository {



    Optional<Restaurant> findRestaurantById(UUID restaurantId);
    Optional<Restaurant> findRestaurantByIdWithLock(UUID restaurantId);

    Restaurant saveRestaurant(Restaurant restaurant);
    
    List<Restaurant> findAllByIdIn(List<UUID> restaurantIds);
    
    com.berkay.restaurant.service.domain.dto.read.RestaurantPageResult findPublicRestaurants(String name, List<String> cuisineCodes, Boolean available, java.math.BigDecimal maxMinimumOrderAmount, Integer maxDeliveryTime, int page, int size);
}
