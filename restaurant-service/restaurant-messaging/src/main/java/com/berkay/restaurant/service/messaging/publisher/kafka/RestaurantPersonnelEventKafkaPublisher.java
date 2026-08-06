package com.berkay.restaurant.service.messaging.publisher.kafka;

import com.berkay.kafka.order.avro.model.RestaurantPersonnelAvroModel;
import com.berkay.kafka.producer.service.KafkaProducer;
import com.berkay.outbox.OutboxStatus;
import com.berkay.restaurant.service.domain.outbox.model.RestaurantOutboxMessage;
import com.berkay.restaurant.service.domain.outbox.model.RestaurantPersonnelEventPayload;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.function.BiConsumer;

import com.berkay.restaurant.service.domain.RestaurantPersonnelMessagePublisher;

@Slf4j
@Component
public class RestaurantPersonnelEventKafkaPublisher implements RestaurantPersonnelMessagePublisher {

    private final KafkaProducer<String, RestaurantPersonnelAvroModel> kafkaProducer;
    private final String topicName;
    private final ObjectMapper objectMapper;

    public RestaurantPersonnelEventKafkaPublisher(KafkaProducer<String, RestaurantPersonnelAvroModel> kafkaProducer,
                                                  @Value("${restaurant-service.restaurant-personnel-topic-name:restaurant-personnel}") String topicName,
                                                  ObjectMapper objectMapper) {
        this.kafkaProducer = kafkaProducer;
        this.topicName = topicName;
        this.objectMapper = objectMapper;
    }

    @Override
    public void publish(RestaurantOutboxMessage restaurantOutboxMessage,
                        BiConsumer<RestaurantOutboxMessage, OutboxStatus> outboxCallback) {
        try {
            RestaurantPersonnelEventPayload payload = objectMapper.readValue(
                    restaurantOutboxMessage.getPayload(),
                    RestaurantPersonnelEventPayload.class
            );

            log.info("Received RestaurantOutboxMessage for personnel addition. restaurant id: {} and user id: {}, outbox id: {}",
                    payload.getRestaurantId(), payload.getUserId(), restaurantOutboxMessage.getId());

            RestaurantPersonnelAvroModel avroModel = RestaurantPersonnelAvroModel.newBuilder()
                    .setRestaurantId(UUID.fromString(payload.getRestaurantId()))
                    .setUserId(UUID.fromString(payload.getUserId()))
                    .setAddedByMerchantId(UUID.fromString(payload.getAddedByMerchantId()))
                    .setCreatedAt(payload.getCreatedAt().toInstant())
                    .setEventType(payload.getEventType())
                    .build();

            kafkaProducer.send(topicName,
                    payload.getRestaurantId(),
                    avroModel,
                    (result, ex) -> {
                        if (ex == null) {
                            outboxCallback.accept(restaurantOutboxMessage, OutboxStatus.COMPLETED);
                        } else {
                            log.error("Failed to send RestaurantPersonnelEvent to Kafka", ex);
                            outboxCallback.accept(restaurantOutboxMessage, OutboxStatus.FAILED);
                        }
                    });

            log.info("RestaurantPersonnelEvent sent to Kafka for restaurant id: {} and user id: {}",
                    avroModel.getRestaurantId(), avroModel.getUserId());
        } catch (Exception e) {
            log.error("Error while sending RestaurantPersonnelEvent message to kafka with outbox id: {}, error: {}",
                    restaurantOutboxMessage.getId(), e.getMessage());
            outboxCallback.accept(restaurantOutboxMessage, OutboxStatus.FAILED);
        }
    }
}
