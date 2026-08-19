package com.berkay.restaurant.service.dataaccess.restaurant.repository;

import com.berkay.restaurant.service.dataaccess.restaurant.entity.RestaurantPersonnelEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;
import java.util.List;

@Repository
public interface RestaurantPersonnelJpaRepository extends JpaRepository<RestaurantPersonnelEntity, UUID> {
    List<RestaurantPersonnelEntity> findByRestaurantId(UUID restaurantId);
    List<RestaurantPersonnelEntity> findByUserId(UUID userId);
    boolean existsByRestaurantIdAndUserId(UUID restaurantId, UUID userId);
    void deleteByRestaurantIdAndUserId(UUID restaurantId, UUID userId);
}
