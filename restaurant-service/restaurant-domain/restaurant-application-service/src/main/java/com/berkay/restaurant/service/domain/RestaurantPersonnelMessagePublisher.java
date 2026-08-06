package com.berkay.restaurant.service.domain;

import com.berkay.outbox.OutboxStatus;
import com.berkay.restaurant.service.domain.outbox.model.RestaurantOutboxMessage;

import java.util.function.BiConsumer;

public interface RestaurantPersonnelMessagePublisher {
    void publish(RestaurantOutboxMessage restaurantOutboxMessage,
                 BiConsumer<RestaurantOutboxMessage, OutboxStatus> outboxCallback);
}
