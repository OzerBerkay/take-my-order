package com.berkay.identity.service.ports.output.repository;

import com.berkay.identity.service.outbox.model.UserOutboxMessage;
import com.berkay.outbox.OutboxStatus;

import java.util.List;

public interface UserOutboxRepository {
    UserOutboxMessage save(UserOutboxMessage userOutboxMessage);

    List<UserOutboxMessage> findByOutboxStatus(OutboxStatus status);

    void deleteByOutboxStatus(OutboxStatus status);
}