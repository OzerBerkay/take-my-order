package com.berkay.identity.service.messaging.mapper;

import com.berkay.identity.service.outbox.model.role.RoleEventPayload;
import com.berkay.kafka.identity.avro.model.PermissionAvroModel;
import com.berkay.kafka.identity.avro.model.RoleEventAvroModel;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class RoleMessagingDataMapper {

    // sagaId yerine messageId kullanıldı ve türü UUID olarak düzeltildi.
    public RoleEventAvroModel roleEventPayloadToRoleEventAvroModel(UUID messageId, RoleEventPayload payload) {

        return RoleEventAvroModel.newBuilder()
                .setId(messageId) // Avro modelindeki UUID beklentisi karşılandı
                .setRoleId(payload.getRole().getId()) // Payload'dan gelen rolün asıl UUID'si
                .setName(payload.getRole().getName())
                .setUserType(payload.getRole().getUserType())
                .setOrganizationalUnitId(payload.getRole().getOrganizationalUnitId())
                .setVersion(payload.getRole().getVersion())
                .setEventType(payload.getEventType())
                .setPermissions(payload.getRole().getPermissions() != null ?
                        payload.getRole().getPermissions().stream()
                                .map(p -> PermissionAvroModel.newBuilder()
                                        .setId(p.getId())
                                        .setCode(p.getCode())
                                        .setDomain(p.getDomain())
                                        .setActive(p.getIsActive())
                                        .setIsRestricted(p.getIsRestricted())
                                        .setCreatedAt(p.getCreatedAt())
                                        .setUpdatedAt(p.getUpdatedAt())
                                        .build())
                                .collect(Collectors.toList()) : null)
                .build();
    }
}