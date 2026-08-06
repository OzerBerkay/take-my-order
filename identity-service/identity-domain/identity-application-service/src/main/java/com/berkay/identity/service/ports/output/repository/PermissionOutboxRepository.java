package com.berkay.identity.service.ports.output.repository;

import com.berkay.identity.service.outbox.model.permission.PermissionOutboxMessage;
import com.berkay.outbox.OutboxStatus;

import java.util.List;
import java.util.Optional;

public interface PermissionOutboxRepository {
    PermissionOutboxMessage save(PermissionOutboxMessage permissionOutboxMessage);
    Optional<List<PermissionOutboxMessage>> findByOutboxStatus(OutboxStatus status);
    void deleteByOutboxStatus(OutboxStatus status);
}
