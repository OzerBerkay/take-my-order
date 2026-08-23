package com.berkay.order.service.domain.ports.output.repository;

import com.berkay.domain.valueobject.OrderId;
import com.berkay.order.service.domain.entity.Order;
import com.berkay.order.service.domain.valueobject.TrackingId;

import java.util.Optional;

public interface OrderRepository {

    Order save(Order order);

    Optional<Order> findById(OrderId orderId);

    Optional<Order> findByTrackingId(TrackingId trackingId);

    com.berkay.order.service.domain.dto.read.OrderPageResult findByCustomerId(com.berkay.domain.valueobject.CustomerId customerId, int page, int size);
    
    Optional<java.util.List<Order>> findByRestaurantIdAndOrderStatus(com.berkay.domain.valueobject.RestaurantId restaurantId, com.berkay.domain.valueobject.OrderStatus orderStatus);
}
