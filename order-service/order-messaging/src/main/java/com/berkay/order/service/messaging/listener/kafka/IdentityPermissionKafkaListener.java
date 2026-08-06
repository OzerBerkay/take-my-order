package com.berkay.order.service.messaging.listener.kafka;

import com.berkay.kafka.consumer.KafkaConsumer;
import com.berkay.kafka.identity.avro.model.PermissionEventAvroModel;
import com.berkay.order.service.domain.dto.message.PermissionEventPayload;
import com.berkay.order.service.domain.dto.message.PermissionPayload;
import com.berkay.order.service.domain.ports.input.message.listener.permission.PermissionMessageListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class IdentityPermissionKafkaListener implements KafkaConsumer<PermissionEventAvroModel> {

    private final PermissionMessageListener permissionMessageListener;

    public IdentityPermissionKafkaListener(PermissionMessageListener permissionMessageListener) {
        this.permissionMessageListener = permissionMessageListener;
    }

    @Override
    @KafkaListener(id = "${kafka-consumer-config.permission-consumer-group-id}",
            topics = "${order-service.permission-topic-name}")
    public void receive(@Payload List<PermissionEventAvroModel> messages,
                        @Header(KafkaHeaders.RECEIVED_KEY) List<String> keys,
                        @Header(KafkaHeaders.RECEIVED_PARTITION) List<Integer> partitions,
                        @Header(KafkaHeaders.OFFSET) List<Long> offsets) {

        log.info("{} number of permission events received with keys:{}, partitions:{} and offsets: {}",
                messages.size(), keys.toString(), partitions.toString(), offsets.toString());

        messages.forEach(avroModel -> {
            PermissionPayload permissionPayload = PermissionPayload.builder()
                    .id(avroModel.getPermissionId())
                    .code(avroModel.getCode())
                    .domain(avroModel.getDomain())
                    .isActive(avroModel.getActive())
                    .isRestricted(avroModel.getIsRestricted())
                    .createdAt(avroModel.getCreatedAt() != null ? avroModel.getCreatedAt().toString() : null)
                    .updatedAt(avroModel.getUpdatedAt() != null ? avroModel.getUpdatedAt().toString() : null)
                    .build();

            PermissionEventPayload payload = PermissionEventPayload.builder()
                    .eventType("PERMISSION_UPDATED")
                    .permission(permissionPayload)
                    .build();

            permissionMessageListener.permissionUpdated(payload);
        });
    }
}
