package com.berkay.identity.service.dataaccess.outbox.mapper;

import com.berkay.identity.service.dataaccess.outbox.entity.RoleOutboxEntity;
import com.berkay.identity.service.outbox.model.role.RoleOutboxMessage;
import org.springframework.stereotype.Component;

@Component
public class RoleOutboxDataAccessMapper {

    public RoleOutboxEntity roleOutboxMessageToOutboxEntity(RoleOutboxMessage message) {
        return RoleOutboxEntity.builder()
                .id(message.getId())
                .createdAt(message.getCreatedAt())
                .processedAt(message.getProcessedAt())
                .type(message.getType())
                .payload(message.getPayload())
                .outboxStatus(message.getOutboxStatus())
                .version(message.getVersion())
                .build();
    }

    public RoleOutboxMessage roleOutboxEntityToOutboxMessage(RoleOutboxEntity entity) {
        return RoleOutboxMessage.builder()
                .id(entity.getId())
                .createdAt(entity.getCreatedAt())
                .processedAt(entity.getProcessedAt())
                .type(entity.getType())
                .payload(entity.getPayload())
                .outboxStatus(entity.getOutboxStatus())
                .version(entity.getVersion())
                .build();
    }
}