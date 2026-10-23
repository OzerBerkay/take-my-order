package com.berkay.order.service.domain.ports.output.repository;

import com.berkay.domain.valueobject.OrderStatus;

import java.util.UUID;

public interface OrderAuthProjection {
    UUID getCustomerId();
    UUID getRestaurantId();
    OrderStatus getOrderStatus();
}
