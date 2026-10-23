package com.berkay.order.service.domain.ports.output.message.publisher.delivery;

import com.berkay.order.service.domain.outbox.model.delivery.DeliveryOutboxMessage;
import com.berkay.outbox.OutboxStatus;

import java.util.function.BiConsumer;

public interface DeliveryMessagePublisher {
    void publish(DeliveryOutboxMessage deliveryOutboxMessage,
                 BiConsumer<DeliveryOutboxMessage, OutboxStatus> outboxCallback);
}
