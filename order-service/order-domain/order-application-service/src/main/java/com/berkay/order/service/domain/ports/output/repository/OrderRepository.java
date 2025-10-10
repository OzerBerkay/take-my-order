package com.berkay.order.service.domain.ports.output.repository;

import com.berkay.domain.valueobject.OrderId;
import com.berkay.order.service.domain.entity.Order;
import com.berkay.order.service.domain.valueobject.TrackingId;

import java.util.Optional;

public interface OrderRepository {

    Order save(Order order);

    Optional<Order> findById(OrderId orderId);

    Optional<Order> findByTrackingId(TrackingId trackingId);
}
