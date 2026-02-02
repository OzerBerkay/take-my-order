package com.berkay.identity.service.ports.output.message.publisher;

import com.berkay.identity.service.outbox.model.UserOutboxMessage;
import com.berkay.outbox.OutboxStatus;

import java.util.function.BiConsumer;

public interface UserMessagePublisher {
    void publish(UserOutboxMessage userOutboxMessage,
                 BiConsumer<UserOutboxMessage, OutboxStatus> outboxCallback);
}