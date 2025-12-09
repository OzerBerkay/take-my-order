package com.berkay.customer.service.ports.output.message.publisher;

import com.berkay.customer.service.outbox.model.CustomerOutboxMessage;
import com.berkay.outbox.OutboxStatus;

import java.util.function.BiConsumer;

public interface CustomerMessagePublisher {

    void publish(CustomerOutboxMessage customerOutboxMessage,
                 BiConsumer<CustomerOutboxMessage, OutboxStatus> outboxCallback);

}