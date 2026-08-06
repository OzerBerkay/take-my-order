package com.berkay.identity.service.ports.output.message.publisher;

import com.berkay.identity.service.outbox.model.role.RoleOutboxMessage;
import com.berkay.outbox.OutboxStatus;

import java.util.function.BiConsumer;

public interface RoleMessagePublisher {
    void publish(RoleOutboxMessage roleOutboxMessage,
                 BiConsumer<RoleOutboxMessage, OutboxStatus> outboxCallback);
}