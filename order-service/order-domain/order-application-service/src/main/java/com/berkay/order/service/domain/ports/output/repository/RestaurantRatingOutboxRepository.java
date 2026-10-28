package com.berkay.order.service.domain.ports.output.repository;

import com.berkay.order.service.domain.outbox.model.rating.RestaurantRatingOutboxMessage;
import com.berkay.outbox.OutboxStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RestaurantRatingOutboxRepository {
    RestaurantRatingOutboxMessage save(RestaurantRatingOutboxMessage message);
    Optional<List<RestaurantRatingOutboxMessage>> findByTypeAndOutboxStatus(String type, OutboxStatus outboxStatus);
    Optional<RestaurantRatingOutboxMessage> findByTypeAndRestaurantIdAndOutboxStatus(String type, UUID restaurantId, OutboxStatus outboxStatus);
    void deleteByTypeAndOutboxStatus(String type, OutboxStatus outboxStatus);
}
