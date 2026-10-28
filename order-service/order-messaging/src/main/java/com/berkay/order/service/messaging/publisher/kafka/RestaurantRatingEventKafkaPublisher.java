package com.berkay.order.service.messaging.publisher.kafka;

import com.berkay.kafka.order.avro.model.RestaurantRatingUpdatedAvroModel;
import com.berkay.kafka.producer.KafkaMessageHelper;
import com.berkay.kafka.producer.service.KafkaProducer;
import com.berkay.order.service.domain.config.OrderServiceConfigData;
import com.berkay.order.service.domain.outbox.model.rating.RestaurantRatingEventPayload;
import com.berkay.order.service.domain.outbox.model.rating.RestaurantRatingOutboxMessage;
import com.berkay.order.service.domain.ports.output.message.publisher.rating.RestaurantRatingMessagePublisher;
import com.berkay.order.service.messaging.mapper.OrderMessagingDataMapper;
import com.berkay.outbox.OutboxStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.function.BiConsumer;

@Slf4j
@Component
public class RestaurantRatingEventKafkaPublisher implements RestaurantRatingMessagePublisher {

    private final OrderMessagingDataMapper orderMessagingDataMapper;
    private final KafkaProducer<String, RestaurantRatingUpdatedAvroModel> kafkaProducer;
    private final OrderServiceConfigData orderServiceConfigData;
    private final KafkaMessageHelper kafkaMessageHelper;

    public RestaurantRatingEventKafkaPublisher(OrderMessagingDataMapper orderMessagingDataMapper,
                                               KafkaProducer<String, RestaurantRatingUpdatedAvroModel> kafkaProducer,
                                               OrderServiceConfigData orderServiceConfigData,
                                               KafkaMessageHelper kafkaMessageHelper) {
        this.orderMessagingDataMapper = orderMessagingDataMapper;
        this.kafkaProducer = kafkaProducer;
        this.orderServiceConfigData = orderServiceConfigData;
        this.kafkaMessageHelper = kafkaMessageHelper;
    }

    @Override
    public void publish(RestaurantRatingOutboxMessage message,
                        BiConsumer<RestaurantRatingOutboxMessage, OutboxStatus> outboxCallback) {
        RestaurantRatingEventPayload payload =
                kafkaMessageHelper.getEventPayload(message.getPayload(), RestaurantRatingEventPayload.class);

        String restaurantId = payload.getRestaurantId();
        log.info("Received RestaurantRatingOutboxMessage for restaurant id: {}", restaurantId);

        try {
            RestaurantRatingUpdatedAvroModel avroModel = orderMessagingDataMapper
                    .restaurantRatingEventToRestaurantRatingUpdatedAvroModel(payload);

            kafkaProducer.send(
                    orderServiceConfigData.getRestaurantRatingUpdatedTopicName(),
                    restaurantId,
                    avroModel,
                    kafkaMessageHelper.getKafkaCallback(
                            orderServiceConfigData.getRestaurantRatingUpdatedTopicName(),
                            avroModel,
                            message,
                            outboxCallback,
                            restaurantId,
                            "RestaurantRatingUpdatedAvroModel"));

            log.info("RestaurantRatingUpdatedAvroModel sent to Kafka for restaurant id: {}", restaurantId);
        } catch (Exception e) {
            log.error("Error while sending RestaurantRatingUpdatedAvroModel to Kafka for restaurant id: {}, error: {}",
                    restaurantId, e.getMessage());
        }
    }
}
