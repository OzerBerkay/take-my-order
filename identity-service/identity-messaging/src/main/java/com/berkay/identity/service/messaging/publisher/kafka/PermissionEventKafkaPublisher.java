package com.berkay.identity.service.messaging.publisher.kafka;

import com.berkay.identity.service.config.IdentityServiceConfigData;
import com.berkay.identity.service.messaging.mapper.PermissionMessagingDataMapper;
import com.berkay.identity.service.outbox.model.permission.PermissionEventPayload;
import com.berkay.identity.service.outbox.model.permission.PermissionOutboxMessage;
import com.berkay.identity.service.ports.output.message.publisher.PermissionMessagePublisher;
import com.berkay.kafka.identity.avro.model.PermissionEventAvroModel;
import com.berkay.kafka.producer.KafkaMessageHelper;
import com.berkay.kafka.producer.service.KafkaProducer;
import com.berkay.outbox.OutboxStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.function.BiConsumer;

@Slf4j
@Component
@RequiredArgsConstructor
public class PermissionEventKafkaPublisher implements PermissionMessagePublisher {

    private final PermissionMessagingDataMapper permissionMessagingDataMapper;
    private final KafkaProducer<String, PermissionEventAvroModel> kafkaProducer;
    private final IdentityServiceConfigData identityServiceConfigData;
    private final KafkaMessageHelper kafkaMessageHelper;

    @Override
    public void publish(PermissionOutboxMessage permissionOutboxMessage,
                        BiConsumer<PermissionOutboxMessage, OutboxStatus> outboxCallback) {

        PermissionEventPayload payload = kafkaMessageHelper.getEventPayload(
                permissionOutboxMessage.getPayload(),
                PermissionEventPayload.class
        );

        UUID messageId = permissionOutboxMessage.getId();
        String kafkaKey = payload.getPermission().getId().toString();

        log.info("Received PermissionOutboxMessage for key: {} and event type: {}",
                kafkaKey, payload.getEventType());

        try {
            PermissionEventAvroModel avroModel = permissionMessagingDataMapper
                    .permissionEventPayloadToPermissionEventAvroModel(messageId, payload);

            kafkaProducer.send(
                    identityServiceConfigData.getPermissionEventsTopicName(),
                    kafkaKey,
                    avroModel,
                    kafkaMessageHelper.getKafkaCallback(
                            identityServiceConfigData.getPermissionEventsTopicName(),
                            avroModel,
                            permissionOutboxMessage,
                            outboxCallback,
                            kafkaKey,
                            "PermissionEventAvroModel"
                    )
            );

            log.info("PermissionEventAvroModel sent to Kafka for permission id: {}", avroModel.getPermissionId());
        } catch (Exception e) {
            log.error("Error while sending PermissionEventAvroModel to Kafka for permission id: {}, error: {}",
                    kafkaKey, e.getMessage());
        }
    }
}
