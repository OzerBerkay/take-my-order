package com.berkay.identity.service.dataaccess.outbox.mapper;

import com.berkay.identity.service.dataaccess.outbox.entity.UserOutboxEntity;
import com.berkay.identity.service.outbox.model.UserOutboxMessage;
import org.springframework.stereotype.Component;

@Component
public class UserOutboxDataAccessMapper {

    public UserOutboxEntity outboxMessageToOutboxEntity(UserOutboxMessage userOutboxMessage) {
        return UserOutboxEntity.builder()
                .id(userOutboxMessage.getId())
                .createdAt(userOutboxMessage.getCreatedAt())
                .processedAt(userOutboxMessage.getProcessedAt())
                .type(userOutboxMessage.getType())
                .payload(userOutboxMessage.getPayload())
                .outboxStatus(userOutboxMessage.getOutboxStatus())
                .version(userOutboxMessage.getVersion())
                .build();
    }

    public UserOutboxMessage outboxEntityToOutboxMessage(UserOutboxEntity userOutboxEntity) {
        return UserOutboxMessage.builder()
                .id(userOutboxEntity.getId())
                .createdAt(userOutboxEntity.getCreatedAt())
                .processedAt(userOutboxEntity.getProcessedAt())
                .type(userOutboxEntity.getType())
                .payload(userOutboxEntity.getPayload())
                .outboxStatus(userOutboxEntity.getOutboxStatus())
                .version(userOutboxEntity.getVersion())
                .build();
    }
}