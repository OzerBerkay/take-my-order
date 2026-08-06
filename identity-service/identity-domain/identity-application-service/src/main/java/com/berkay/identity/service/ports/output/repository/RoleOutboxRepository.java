package com.berkay.identity.service.ports.output.repository;

import com.berkay.identity.service.outbox.model.role.RoleOutboxMessage;
import com.berkay.outbox.OutboxStatus;

import java.util.List;


public interface RoleOutboxRepository {
    RoleOutboxMessage save(RoleOutboxMessage roleOutboxMessage);

    List<RoleOutboxMessage> findByOutboxStatus(OutboxStatus outboxStatus);
    void deleteByOutboxStatus(OutboxStatus outboxStatus);
}