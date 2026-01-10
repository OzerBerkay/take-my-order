package com.berkay.restaurant.service.dataaccess.restaurant.repository;

import com.berkay.restaurant.service.dataaccess.restaurant.entity.RestaurantEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface RestaurantJpaRepository extends JpaRepository<RestaurantEntity, UUID> {
}