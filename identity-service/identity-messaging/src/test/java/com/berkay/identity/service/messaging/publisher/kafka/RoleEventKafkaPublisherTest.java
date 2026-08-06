package com.berkay.identity.service.messaging.publisher.kafka;

import com.berkay.identity.service.config.IdentityServiceConfigData;
import com.berkay.identity.service.messaging.mapper.RoleMessagingDataMapper;
import com.berkay.identity.service.outbox.model.role.RoleEventPayload;
import com.berkay.identity.service.outbox.model.role.RoleOutboxMessage;
import com.berkay.kafka.identity.avro.model.RoleEventAvroModel;
import com.berkay.kafka.producer.KafkaMessageHelper;
import com.berkay.kafka.producer.service.KafkaProducer;
import com.berkay.outbox.OutboxStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;
import java.util.function.BiConsumer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoleEventKafkaPublisherTest {

    @Mock
    private RoleMessagingDataMapper roleMessagingDataMapper;

    @Mock
    private KafkaProducer<String, RoleEventAvroModel> kafkaProducer;

    @Mock
    private IdentityServiceConfigData identityServiceConfigData;

    @Mock
    private KafkaMessageHelper kafkaMessageHelper;

    @InjectMocks
    private RoleEventKafkaPublisher roleEventKafkaPublisher;

    @Test
    void shouldCallOutboxCallbackWithFailedStatusWhenDeserializationFails() {
        // Arrange
        RoleOutboxMessage message = RoleOutboxMessage.builder()
                .id(UUID.randomUUID())
                .payload("invalid json payload")
                .outboxStatus(OutboxStatus.STARTED)
                .build();

        BiConsumer<RoleOutboxMessage, OutboxStatus> outboxCallback = mock(BiConsumer.class);

        // Simulate Jackson exception / RuntimeException from helper
        when(kafkaMessageHelper.getEventPayload(anyString(), eq(RoleEventPayload.class)))
                .thenThrow(new RuntimeException("JSON Deserialization error"));

        // Act
        roleEventKafkaPublisher.publish(message, outboxCallback);

        // Assert
        verify(outboxCallback, times(1)).accept(message, OutboxStatus.FAILED);
        verifyNoInteractions(kafkaProducer);
    }
}
