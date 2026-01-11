package com.berkay.order.service.dataaccess.restaurant.repository;

import com.berkay.order.service.dataaccess.restaurant.entity.RestaurantEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RestaurantJpaRepository extends JpaRepository<RestaurantEntity, UUID> {

    // Sipariş validasyonu için: Belirli bir restoranı ve içindeki SADECE istenen ürünleri getir.
    Optional<RestaurantEntity> findByRestaurantIdAndProducts_ProductIdIn(UUID restaurantId, List<UUID> productIds);
}
