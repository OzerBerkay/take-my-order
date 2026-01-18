package com.berkay.order.service.dataaccess.restaurant.repository;

import com.berkay.order.service.dataaccess.restaurant.entity.RestaurantEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RestaurantJpaRepository extends JpaRepository<RestaurantEntity, UUID> {

    // Sipariş validasyonu için: Belirli bir restoranı ve içindeki SADECE istenen ürünleri getir.
    @Query("SELECT DISTINCT r FROM RestaurantEntity r " +
            "JOIN FETCH r.products p " +
            "WHERE r.restaurantId = :restaurantId " +
            "AND p.productId IN :productIds")
    Optional<RestaurantEntity> findByRestaurantIdAndProductIds(
            @Param("restaurantId") UUID restaurantId,
            @Param("productIds") List<UUID> productIds);
}
