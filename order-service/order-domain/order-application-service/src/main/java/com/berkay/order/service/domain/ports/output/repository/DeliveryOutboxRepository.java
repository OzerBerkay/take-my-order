package com.berkay.order.service.domain.ports.output.repository;

import com.berkay.order.service.domain.outbox.model.delivery.DeliveryOutboxMessage;
import com.berkay.outbox.OutboxStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DeliveryOutboxRepository {
    DeliveryOutboxMessage save(DeliveryOutboxMessage deliveryOutboxMessage);
    Optional<List<DeliveryOutboxMessage>> findByTypeAndOutboxStatus(String type, OutboxStatus outboxStatus);
    Optional<DeliveryOutboxMessage> findByOrderId(UUID orderId);
    void deleteByTypeAndOutboxStatus(String type, OutboxStatus outboxStatus);
}
