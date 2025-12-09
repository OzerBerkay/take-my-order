package com.berkay.customer.service.ports.output.repository;

import com.berkay.customer.service.outbox.model.CustomerOutboxMessage;
import com.berkay.outbox.OutboxStatus;

import java.util.List;
import java.util.Optional;

public interface CustomerOutboxRepository {

    CustomerOutboxMessage save(CustomerOutboxMessage customerOutboxMessage);

    Optional<List<CustomerOutboxMessage>> findByTypeAndOutboxStatus(String type, OutboxStatus outboxStatus);

    void deleteByTypeAndOutboxStatus(String type, OutboxStatus outboxStatus);
}
