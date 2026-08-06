package com.berkay.identity.service.messaging.publisher.kafka;

import com.berkay.identity.service.config.IdentityServiceConfigData;
import com.berkay.identity.service.messaging.mapper.RoleMessagingDataMapper;
import com.berkay.identity.service.outbox.model.role.RoleEventPayload;
import com.berkay.identity.service.outbox.model.role.RoleOutboxMessage;
import com.berkay.identity.service.ports.output.message.publisher.RoleMessagePublisher;
import com.berkay.kafka.identity.avro.model.RoleEventAvroModel;
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
public class RoleEventKafkaPublisher implements RoleMessagePublisher {

    private final RoleMessagingDataMapper roleMessagingDataMapper;
    private final KafkaProducer<String, RoleEventAvroModel> kafkaProducer;
    private final IdentityServiceConfigData identityServiceConfigData;
    private final KafkaMessageHelper kafkaMessageHelper;

    // NOT: ObjectMapper enjeksiyonu ve özel parse metodu kaldırıldı!

    @Override
    public void publish(RoleOutboxMessage roleOutboxMessage,
                        BiConsumer<RoleOutboxMessage, OutboxStatus> outboxCallback) {
        String kafkaKey = "unknown";

        try {
            // Altyapıdan gelen merkezi getEventPayload metodunu kullanıyoruz
            RoleEventPayload roleEventPayload = kafkaMessageHelper.getEventPayload(
                    roleOutboxMessage.getPayload(),
                    RoleEventPayload.class
            );

            UUID messageId = roleOutboxMessage.getId();
            kafkaKey = roleEventPayload.getRole().getId().toString();

            log.info("Received RoleOutboxMessage for key: {} and event type: {}",
                    kafkaKey, roleEventPayload.getEventType());

            // Payload -> Avro Dönüşümü
            RoleEventAvroModel roleEventAvroModel = roleMessagingDataMapper
                    .roleEventPayloadToRoleEventAvroModel(messageId, roleEventPayload);

            // Kafka'ya Gönder (Helper Callback'i ile Birlikte)
            kafkaProducer.send(
                    identityServiceConfigData.getRoleEventsTopicName(),
                    kafkaKey,
                    roleEventAvroModel,
                    kafkaMessageHelper.getKafkaCallback(
                            identityServiceConfigData.getRoleEventsTopicName(),
                            roleEventAvroModel,
                            roleOutboxMessage,
                            outboxCallback,
                            kafkaKey,
                            "RoleEventAvroModel"
                    )
            );

            log.info("RoleEventAvroModel sent to Kafka for role id: {}", roleEventAvroModel.getRoleId());
        } catch (Exception e) {
            log.error("Error while sending RoleEventAvroModel to Kafka for role id: {}, error: {}",
                    kafkaKey, e.getMessage(), e);
            outboxCallback.accept(roleOutboxMessage, OutboxStatus.FAILED);
        }
    }
}