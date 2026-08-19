package com.berkay.restaurant.service.dataaccess.restaurant.repository;

import com.berkay.restaurant.service.dataaccess.restaurant.entity.RestaurantEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.berkay.restaurant.service.domain.valueobject.CuisineType;

import java.util.List;
import java.util.UUID;

@Repository
public interface RestaurantJpaRepository extends JpaRepository<RestaurantEntity, UUID> {
    
    List<RestaurantEntity> findAllByRestaurantIdIn(List<UUID> restaurantIds);

    @Query("SELECT r FROM RestaurantEntity r WHERE r.isActive = true " +
           "AND (CAST(:name AS string) IS NULL OR LOWER(r.restaurantName) LIKE LOWER(CONCAT('%', CAST(:name AS string), '%'))) " +
           "AND (CAST(:cuisineType AS string) IS NULL OR r.cuisineType = :cuisineType) " +
           "AND (:available IS NULL OR r.available = :available) " +
           "ORDER BY r.available DESC, r.restaurantName ASC")
    Page<RestaurantEntity> findPublicRestaurants(@Param("name") String name, @Param("cuisineType") CuisineType cuisineType, @Param("available") Boolean available, Pageable pageable);
}