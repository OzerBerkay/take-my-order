package com.berkay.restaurant.service.domain.ports.output.repository;

import com.berkay.outbox.OutboxStatus;
import com.berkay.restaurant.service.domain.outbox.model.RestaurantOutboxMessage;

import java.util.List;
import java.util.Optional;

public interface RestaurantOutboxRepository {
    RestaurantOutboxMessage save(RestaurantOutboxMessage restaurantOutboxMessage);

    List<RestaurantOutboxMessage> findByTypeAndOutboxStatus(List<String> types, OutboxStatus outboxStatus);

    void deleteByTypeAndOutboxStatus(List<String> types, OutboxStatus outboxStatus);
}