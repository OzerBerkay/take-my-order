package com.berkay.order.service.messaging.publisher.kafka;

import com.berkay.kafka.order.avro.model.OrderDeliveredAvroModel;
import com.berkay.kafka.producer.KafkaMessageHelper;
import com.berkay.kafka.producer.service.KafkaProducer;
import com.berkay.order.service.domain.config.OrderServiceConfigData;
import com.berkay.order.service.domain.outbox.model.delivery.DeliveryOutboxMessage;
import com.berkay.order.service.domain.outbox.model.delivery.OrderDeliveredEventPayload;
import com.berkay.order.service.messaging.mapper.OrderMessagingDataMapper;
import com.berkay.outbox.OutboxStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class OrderDeliveredEventKafkaPublisherTest {

    @Mock
    private KafkaProducer<String, OrderDeliveredAvroModel> kafkaProducer;

    @Mock
    private OrderServiceConfigData orderServiceConfigData;

    @Mock
    private KafkaMessageHelper kafkaMessageHelper;

    private OrderMessagingDataMapper orderMessagingDataMapper;
    private OrderDeliveredEventKafkaPublisher publisher;

    private final UUID orderId = UUID.randomUUID();
    private final UUID restaurantId = UUID.randomUUID();
    private final UUID customerId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        orderMessagingDataMapper = new OrderMessagingDataMapper();
        publisher = new OrderDeliveredEventKafkaPublisher(
                orderMessagingDataMapper,
                kafkaProducer,
                orderServiceConfigData,
                kafkaMessageHelper
        );
    }

    @Test
    void testPublish_SendsToKafka() {
        OrderDeliveredEventPayload payload = OrderDeliveredEventPayload.builder()
                .orderId(orderId)
                .restaurantId(restaurantId)
                .customerId(customerId)
                .price(new BigDecimal("150.00"))
                .createdAt(ZonedDateTime.now())
                .build();

        DeliveryOutboxMessage message = DeliveryOutboxMessage.builder()
                .id(UUID.randomUUID())
                .orderId(orderId)
                .type("OrderDelivery")
                .payload("{}")
                .outboxStatus(OutboxStatus.STARTED)
                .build();

        when(kafkaMessageHelper.getEventPayload("{}", OrderDeliveredEventPayload.class))
                .thenReturn(payload);
        when(orderServiceConfigData.getOrderDeliveredTopicName())
                .thenReturn("order-delivered");

        publisher.publish(message, (m, s) -> {});

        verify(kafkaProducer).send(
                eq("order-delivered"),
                eq(orderId.toString()),
                any(OrderDeliveredAvroModel.class),
                any()
        );
    }
}
