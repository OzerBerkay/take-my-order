package com.berkay.order.service.domain.outbox.scheduler.delivery;

import com.berkay.order.service.domain.exception.OrderDomainException;
import com.berkay.order.service.domain.outbox.model.delivery.DeliveryOutboxMessage;
import com.berkay.order.service.domain.outbox.model.delivery.OrderDeliveredEventPayload;
import com.berkay.order.service.domain.ports.output.repository.DeliveryOutboxRepository;
import com.berkay.outbox.OutboxStatus;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Component
public class DeliveryOutboxHelper {

    public static final String ORDER_DELIVERY_EVENT_TYPE = "OrderDelivery";

    private final DeliveryOutboxRepository deliveryOutboxRepository;
    private final ObjectMapper objectMapper;

    public DeliveryOutboxHelper(DeliveryOutboxRepository deliveryOutboxRepository,
                                ObjectMapper objectMapper) {
        this.deliveryOutboxRepository = deliveryOutboxRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public Optional<List<DeliveryOutboxMessage>> getDeliveryOutboxMessagesByOutboxStatus(OutboxStatus outboxStatus) {
        return deliveryOutboxRepository.findByTypeAndOutboxStatus(ORDER_DELIVERY_EVENT_TYPE, outboxStatus);
    }

    @Transactional
    public void save(DeliveryOutboxMessage deliveryOutboxMessage) {
        DeliveryOutboxMessage response = deliveryOutboxRepository.save(deliveryOutboxMessage);
        if (response == null) {
            log.error("Could not save DeliveryOutboxMessage with outbox id: {}", deliveryOutboxMessage.getId());
            throw new OrderDomainException("Could not save DeliveryOutboxMessage with outbox id: " +
                    deliveryOutboxMessage.getId());
        }
        log.info("DeliveryOutboxMessage saved with outbox id: {}", deliveryOutboxMessage.getId());
    }

    @Transactional
    public void saveDeliveryOutboxMessage(OrderDeliveredEventPayload eventPayload,
                                          OutboxStatus outboxStatus) {
        save(DeliveryOutboxMessage.builder()
                .id(UUID.randomUUID())
                .orderId(eventPayload.getOrderId())
                .createdAt(eventPayload.getCreatedAt())
                .type(ORDER_DELIVERY_EVENT_TYPE)
                .payload(createPayload(eventPayload))
                .outboxStatus(outboxStatus)
                .build());
    }

    @Transactional
    public void deleteDeliveryOutboxMessageByOutboxStatus(OutboxStatus outboxStatus) {
        deliveryOutboxRepository.deleteByTypeAndOutboxStatus(ORDER_DELIVERY_EVENT_TYPE, outboxStatus);
    }

    private String createPayload(OrderDeliveredEventPayload eventPayload) {
        try {
            return objectMapper.writeValueAsString(eventPayload);
        } catch (JsonProcessingException e) {
            log.error("Could not create OrderDeliveredEventPayload object for order id: {}",
                    eventPayload.getOrderId(), e);
            throw new OrderDomainException("Could not create OrderDeliveredEventPayload object for order id: " +
                    eventPayload.getOrderId(), e);
        }
    }
}
