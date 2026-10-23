package com.berkay.order.service.dataaccess.outbox.delivery.repository;

import com.berkay.order.service.dataaccess.outbox.delivery.entity.DeliveryOutboxEntity;
import com.berkay.outbox.OutboxStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DeliveryOutboxJpaRepository extends JpaRepository<DeliveryOutboxEntity, UUID> {

    Optional<List<DeliveryOutboxEntity>> findByTypeAndOutboxStatus(String type, OutboxStatus outboxStatus);

    Optional<DeliveryOutboxEntity> findByOrderId(UUID orderId);

    void deleteByTypeAndOutboxStatus(String type, OutboxStatus outboxStatus);
}
