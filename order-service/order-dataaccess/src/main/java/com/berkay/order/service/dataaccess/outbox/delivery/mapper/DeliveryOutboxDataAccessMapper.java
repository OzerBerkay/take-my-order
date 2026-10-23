package com.berkay.order.service.dataaccess.outbox.delivery.mapper;

import com.berkay.order.service.dataaccess.outbox.delivery.entity.DeliveryOutboxEntity;
import com.berkay.order.service.domain.outbox.model.delivery.DeliveryOutboxMessage;
import org.springframework.stereotype.Component;

@Component
public class DeliveryOutboxDataAccessMapper {

    public DeliveryOutboxEntity deliveryOutboxMessageToDeliveryOutboxEntity(DeliveryOutboxMessage message) {
        return DeliveryOutboxEntity.builder()
                .id(message.getId())
                .orderId(message.getOrderId())
                .createdAt(message.getCreatedAt())
                .processedAt(message.getProcessedAt())
                .type(message.getType())
                .payload(message.getPayload())
                .outboxStatus(message.getOutboxStatus())
                .version(message.getVersion())
                .build();
    }

    public DeliveryOutboxMessage deliveryOutboxEntityToDeliveryOutboxMessage(DeliveryOutboxEntity entity) {
        return DeliveryOutboxMessage.builder()
                .id(entity.getId())
                .orderId(entity.getOrderId())
                .createdAt(entity.getCreatedAt())
                .processedAt(entity.getProcessedAt())
                .type(entity.getType())
                .payload(entity.getPayload())
                .outboxStatus(entity.getOutboxStatus())
                .version(entity.getVersion())
                .build();
    }
}
