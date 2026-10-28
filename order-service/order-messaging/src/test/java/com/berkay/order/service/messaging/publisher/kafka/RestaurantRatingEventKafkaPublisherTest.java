package com.berkay.order.service.messaging.publisher.kafka;

import com.berkay.kafka.order.avro.model.RestaurantRatingUpdatedAvroModel;
import com.berkay.kafka.producer.KafkaMessageHelper;
import com.berkay.kafka.producer.service.KafkaProducer;
import com.berkay.order.service.domain.config.OrderServiceConfigData;
import com.berkay.order.service.domain.outbox.model.rating.RestaurantRatingEventPayload;
import com.berkay.order.service.domain.outbox.model.rating.RestaurantRatingOutboxMessage;
import com.berkay.order.service.messaging.mapper.OrderMessagingDataMapper;
import com.berkay.outbox.OutboxStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.ZonedDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class RestaurantRatingEventKafkaPublisherTest {

    @Mock
    private KafkaProducer<String, RestaurantRatingUpdatedAvroModel> kafkaProducer;

    @Mock
    private OrderServiceConfigData orderServiceConfigData;

    @Mock
    private KafkaMessageHelper kafkaMessageHelper;

    private OrderMessagingDataMapper orderMessagingDataMapper;
    private RestaurantRatingEventKafkaPublisher publisher;

    private final UUID restaurantId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        orderMessagingDataMapper = new OrderMessagingDataMapper();
        publisher = new RestaurantRatingEventKafkaPublisher(
                orderMessagingDataMapper,
                kafkaProducer,
                orderServiceConfigData,
                kafkaMessageHelper
        );
    }

    @Test
    void testPublish_SendsToKafka() {
        RestaurantRatingEventPayload payload = RestaurantRatingEventPayload.builder()
                .id(UUID.randomUUID().toString())
                .restaurantId(restaurantId.toString())
                .averageRating(4.5)
                .totalReviews(2)
                .updatedAt(ZonedDateTime.now())
                .build();

        RestaurantRatingOutboxMessage message = RestaurantRatingOutboxMessage.builder()
                .id(UUID.randomUUID())
                .restaurantId(restaurantId)
                .type("RestaurantRating")
                .payload("{}")
                .outboxStatus(OutboxStatus.STARTED)
                .build();

        when(kafkaMessageHelper.getEventPayload("{}", RestaurantRatingEventPayload.class))
                .thenReturn(payload);
        when(orderServiceConfigData.getRestaurantRatingUpdatedTopicName())
                .thenReturn("restaurant-rating-updated");

        publisher.publish(message, (m, s) -> {});

        verify(kafkaProducer).send(
                eq("restaurant-rating-updated"),
                eq(restaurantId.toString()),
                any(RestaurantRatingUpdatedAvroModel.class),
                any()
        );
    }
}
