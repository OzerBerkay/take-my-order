package com.berkay.payment.service.domain.ports.output.message.publisher;

import com.berkay.outbox.OutboxStatus;
import com.berkay.payment.service.domain.outbox.model.OrderOutboxMessage;

import java.util.function.BiConsumer;

public interface PaymentResponseMessagePublisher {
    void publish(OrderOutboxMessage orderOutboxMessage,
                 BiConsumer<OrderOutboxMessage, OutboxStatus> outboxCallback);
}
