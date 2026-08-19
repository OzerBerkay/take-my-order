package com.berkay.restaurant.service.domain.ports.output.repository;

import com.berkay.restaurant.service.domain.entity.RestaurantPersonnel;

public interface RestaurantPersonnelRepository {
    RestaurantPersonnel save(RestaurantPersonnel restaurantPersonnel);
    boolean existsByRestaurantIdAndUserId(java.util.UUID restaurantId, java.util.UUID userId);
    void deleteByRestaurantIdAndUserId(java.util.UUID restaurantId, java.util.UUID userId);
}
