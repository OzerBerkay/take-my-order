package com.berkay.order.service.dataaccess.order.repository;

import com.berkay.order.service.dataaccess.order.entity.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;
import java.util.Optional;

public interface OrderJpaRepository extends JpaRepository<OrderEntity, UUID> {
    Optional<OrderEntity> findByTrackingId(UUID trackingId);

    org.springframework.data.domain.Page<OrderEntity> findByCustomerId(UUID customerId, org.springframework.data.domain.Pageable pageable);
    
    Optional<java.util.List<OrderEntity>> findByRestaurantIdAndOrderStatus(UUID restaurantId, com.berkay.domain.valueobject.OrderStatus orderStatus);
}
