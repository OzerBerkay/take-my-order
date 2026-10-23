package com.berkay.order.service.dataaccess.outbox.delivery.adapter;

import com.berkay.order.service.dataaccess.outbox.delivery.entity.DeliveryOutboxEntity;
import com.berkay.order.service.dataaccess.outbox.delivery.mapper.DeliveryOutboxDataAccessMapper;
import com.berkay.order.service.dataaccess.outbox.delivery.repository.DeliveryOutboxJpaRepository;
import com.berkay.order.service.domain.outbox.model.delivery.DeliveryOutboxMessage;
import com.berkay.order.service.domain.ports.output.repository.DeliveryOutboxRepository;
import com.berkay.outbox.OutboxStatus;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class DeliveryOutboxRepositoryImpl implements DeliveryOutboxRepository {

    private final DeliveryOutboxJpaRepository deliveryOutboxJpaRepository;
    private final DeliveryOutboxDataAccessMapper deliveryOutboxDataAccessMapper;

    public DeliveryOutboxRepositoryImpl(DeliveryOutboxJpaRepository deliveryOutboxJpaRepository,
                                        DeliveryOutboxDataAccessMapper deliveryOutboxDataAccessMapper) {
        this.deliveryOutboxJpaRepository = deliveryOutboxJpaRepository;
        this.deliveryOutboxDataAccessMapper = deliveryOutboxDataAccessMapper;
    }

    @Override
    public DeliveryOutboxMessage save(DeliveryOutboxMessage deliveryOutboxMessage) {
        Optional<DeliveryOutboxEntity> existing = deliveryOutboxJpaRepository.findById(deliveryOutboxMessage.getId());
        DeliveryOutboxEntity entity;
        if (existing.isPresent()) {
            entity = existing.get();
            entity.setProcessedAt(deliveryOutboxMessage.getProcessedAt());
            entity.setOutboxStatus(deliveryOutboxMessage.getOutboxStatus());
            entity.setPayload(deliveryOutboxMessage.getPayload());
        } else {
            entity = deliveryOutboxDataAccessMapper.deliveryOutboxMessageToDeliveryOutboxEntity(deliveryOutboxMessage);
        }
        return deliveryOutboxDataAccessMapper.deliveryOutboxEntityToDeliveryOutboxMessage(
                deliveryOutboxJpaRepository.save(entity));
    }

    @Override
    public Optional<List<DeliveryOutboxMessage>> findByTypeAndOutboxStatus(String type, OutboxStatus outboxStatus) {
        return deliveryOutboxJpaRepository.findByTypeAndOutboxStatus(type, outboxStatus)
                .map(list -> list.stream()
                        .map(deliveryOutboxDataAccessMapper::deliveryOutboxEntityToDeliveryOutboxMessage)
                        .collect(Collectors.toList()));
    }

    @Override
    public Optional<DeliveryOutboxMessage> findByOrderId(UUID orderId) {
        return deliveryOutboxJpaRepository.findByOrderId(orderId)
                .map(deliveryOutboxDataAccessMapper::deliveryOutboxEntityToDeliveryOutboxMessage);
    }

    @Override
    public void deleteByTypeAndOutboxStatus(String type, OutboxStatus outboxStatus) {
        deliveryOutboxJpaRepository.deleteByTypeAndOutboxStatus(type, outboxStatus);
    }
}
