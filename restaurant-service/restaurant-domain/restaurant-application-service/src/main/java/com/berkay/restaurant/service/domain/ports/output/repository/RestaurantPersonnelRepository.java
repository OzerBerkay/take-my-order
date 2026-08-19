package com.berkay.restaurant.service.domain.ports.output.repository;

import com.berkay.restaurant.service.domain.entity.RestaurantPersonnel;
import java.util.List;
import java.util.UUID;

public interface RestaurantPersonnelRepository {
    RestaurantPersonnel save(RestaurantPersonnel restaurantPersonnel);
    List<RestaurantPersonnel> findByRestaurantId(UUID restaurantId);
    List<RestaurantPersonnel> findByUserId(UUID userId);
    boolean existsByRestaurantIdAndUserId(java.util.UUID restaurantId, java.util.UUID userId);
    void deleteByRestaurantIdAndUserId(java.util.UUID restaurantId, java.util.UUID userId);
}
