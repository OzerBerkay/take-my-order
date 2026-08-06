package com.berkay.identity.service.messaging.mapper;

import com.berkay.identity.service.outbox.model.permission.PermissionEventPayload;
import com.berkay.kafka.identity.avro.model.PermissionEventAvroModel;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class PermissionMessagingDataMapper {

    public PermissionEventAvroModel permissionEventPayloadToPermissionEventAvroModel(UUID messageId, PermissionEventPayload payload) {
        return PermissionEventAvroModel.newBuilder()
                .setId(messageId)
                .setPermissionId(payload.getPermission().getId())
                .setCode(payload.getPermission().getCode())
                .setDomain(payload.getPermission().getDomain())
                .setActive(payload.getPermission().getIsActive())
                .setIsRestricted(payload.getPermission().getIsRestricted())
                .setCreatedAt(payload.getPermission().getCreatedAt())
                .setUpdatedAt(payload.getPermission().getUpdatedAt())
                .setEventType(payload.getEventType())
                .build();
    }
}
