package com.berkay.restaurant.service.messaging.listener.kafka;

import com.berkay.kafka.consumer.KafkaConsumer;
import com.berkay.kafka.identity.avro.model.RoleEventAvroModel;
import com.berkay.restaurant.service.domain.dto.message.RoleEventPayload;
import com.berkay.restaurant.service.domain.ports.input.message.listener.role.RoleMessageListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
public class IdentityRoleKafkaListener implements KafkaConsumer<RoleEventAvroModel> {

    private final RoleMessageListener roleMessageListener;

    public IdentityRoleKafkaListener(RoleMessageListener roleMessageListener) {
        this.roleMessageListener = roleMessageListener;
    }

    @Override
    @KafkaListener(id = "${kafka-consumer-config.role-consumer-group-id}",
            topics = "${restaurant-service.role-topic-name}")
    public void receive(@Payload List<RoleEventAvroModel> messages,
                        @Header(KafkaHeaders.RECEIVED_KEY) List<String> keys,
                        @Header(KafkaHeaders.RECEIVED_PARTITION) List<Integer> partitions,
                        @Header(KafkaHeaders.OFFSET) List<Long> offsets) {

        log.info("{} number of role events received with keys:{}, partitions:{} and offsets: {}",
                messages.size(), keys.toString(), partitions.toString(), offsets.toString());

        messages.forEach(avroModel -> {
            java.util.List<com.berkay.restaurant.service.domain.dto.message.PermissionPayload> permissions = null;
            if (avroModel.getPermissions() != null) {
                permissions = avroModel.getPermissions().stream()
                        .map(p -> com.berkay.restaurant.service.domain.dto.message.PermissionPayload.builder()
                                .id(p.getId())
                                .code(p.getCode())
                                .domain(p.getDomain())
                                .isActive(p.getActive())
                                .isRestricted(p.getIsRestricted())
                                .createdAt(p.getCreatedAt() != null ? p.getCreatedAt().toString() : null)
                                .updatedAt(p.getUpdatedAt() != null ? p.getUpdatedAt().toString() : null)
                                .build())
                        .toList();
            }

            RoleEventPayload payload = RoleEventPayload.builder()
                    .roleId(avroModel.getRoleId())
                    .name(avroModel.getName())
                    .userType(avroModel.getUserType())
                    .organizationalUnitId(avroModel.getOrganizationalUnitId())
                    .eventType(avroModel.getEventType())
                    .version(avroModel.getVersion())
                    .permissions(permissions)
                    .build();

            if ("ROLE_CREATED".equals(avroModel.getEventType())) {
                roleMessageListener.roleCreated(payload);
            } else if ("ROLE_UPDATED".equals(avroModel.getEventType())) {
                roleMessageListener.roleUpdated(payload);
            } else if ("ROLE_DELETED".equals(avroModel.getEventType())) {
                roleMessageListener.roleDeleted(payload);
            }
        });
    }
}
