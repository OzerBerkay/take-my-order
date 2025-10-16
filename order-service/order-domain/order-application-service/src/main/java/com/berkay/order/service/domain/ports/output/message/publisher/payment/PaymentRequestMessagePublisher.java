package com.berkay.order.service.domain.ports.output.message.publisher.payment;

import com.berkay.order.service.domain.outbox.model.payment.OrderPaymentOutboxMessage;
import com.berkay.outbox.OutboxStatus;

import java.util.function.BiConsumer;

public interface PaymentRequestMessagePublisher {

    void publish(OrderPaymentOutboxMessage orderPaymentOutboxMessage,
                 BiConsumer<OrderPaymentOutboxMessage, OutboxStatus> outboxCallback);
}
