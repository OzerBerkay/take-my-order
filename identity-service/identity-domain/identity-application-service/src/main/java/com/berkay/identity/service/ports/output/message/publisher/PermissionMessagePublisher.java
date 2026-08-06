package com.berkay.identity.service.ports.output.message.publisher;

import com.berkay.identity.service.outbox.model.permission.PermissionOutboxMessage;
import com.berkay.outbox.OutboxStatus;

import java.util.function.BiConsumer;

public interface PermissionMessagePublisher {
    void publish(PermissionOutboxMessage permissionOutboxMessage,
                 BiConsumer<PermissionOutboxMessage, OutboxStatus> outboxCallback);
}
