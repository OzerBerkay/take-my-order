package com.berkay.order.service.domain.ports.output.message.publisher.rating;

import com.berkay.order.service.domain.outbox.model.rating.RestaurantRatingOutboxMessage;
import com.berkay.outbox.OutboxStatus;

import java.util.function.BiConsumer;

public interface RestaurantRatingMessagePublisher {
    void publish(RestaurantRatingOutboxMessage message,
                 BiConsumer<RestaurantRatingOutboxMessage, OutboxStatus> outboxCallback);
}
