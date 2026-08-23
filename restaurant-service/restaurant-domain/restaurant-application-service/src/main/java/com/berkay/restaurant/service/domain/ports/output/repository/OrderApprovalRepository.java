package com.berkay.restaurant.service.domain.ports.output.repository;

import com.berkay.restaurant.service.domain.entity.OrderApproval;

public interface OrderApprovalRepository {
    OrderApproval save(OrderApproval orderApproval);
    java.util.Optional<OrderApproval> findByRestaurantIdAndOrderId(java.util.UUID restaurantId, java.util.UUID orderId);
    java.util.List<OrderApproval> findByRestaurantIdAndStatus(java.util.UUID restaurantId, com.berkay.domain.valueobject.OrderApprovalStatus status);
}
