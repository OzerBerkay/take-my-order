package com.berkay.restaurant.service.messaging.publisher.kafka;

import com.berkay.kafka.order.avro.model.RestaurantCreatedAvroModel;
import com.berkay.kafka.producer.KafkaMessageHelper;
import com.berkay.kafka.producer.service.KafkaProducer;
import com.berkay.outbox.OutboxStatus;
import com.berkay.restaurant.service.domain.config.RestaurantServiceConfigData;
import com.berkay.restaurant.service.domain.outbox.model.RestaurantEventPayload;
import com.berkay.restaurant.service.domain.outbox.model.RestaurantOutboxMessage;
import com.berkay.restaurant.service.domain.ports.output.RestaurantCreatedMessagePublisher;
import com.berkay.restaurant.service.messaging.mapper.RestaurantMessagingDataMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.function.BiConsumer;

@Slf4j
@Component
public class RestaurantCreatedEventKafkaPublisher implements RestaurantCreatedMessagePublisher {

    private final RestaurantMessagingDataMapper restaurantMessagingDataMapper;
    private final KafkaProducer<String, RestaurantCreatedAvroModel> kafkaProducer;
    private final RestaurantServiceConfigData restaurantServiceConfigData;
    private final KafkaMessageHelper kafkaMessageHelper;

    public RestaurantCreatedEventKafkaPublisher(RestaurantMessagingDataMapper restaurantMessagingDataMapper,
                                                KafkaProducer<String, RestaurantCreatedAvroModel> kafkaProducer,
                                                RestaurantServiceConfigData restaurantServiceConfigData,
                                                KafkaMessageHelper kafkaMessageHelper) {
        this.restaurantMessagingDataMapper = restaurantMessagingDataMapper;
        this.kafkaProducer = kafkaProducer;
        this.restaurantServiceConfigData = restaurantServiceConfigData;
        this.kafkaMessageHelper = kafkaMessageHelper;
    }

    @Override
    public void publish(RestaurantOutboxMessage restaurantOutboxMessage,
                        BiConsumer<RestaurantOutboxMessage, OutboxStatus> outboxCallback) {
        RestaurantEventPayload restaurantEventPayload =
                kafkaMessageHelper.getEventPayload(restaurantOutboxMessage.getPayload(),
                        RestaurantEventPayload.class);

        String sagaId = restaurantOutboxMessage.getSagaId().toString();

        log.info("Received RestaurantOutboxMessage for restaurant id: {} and saga id: {}",
                restaurantEventPayload.getRestaurantId(),
                sagaId);

        try {
            RestaurantCreatedAvroModel restaurantCreatedAvroModel =
                    restaurantMessagingDataMapper.restaurantEventPayloadToRestaurantCreatedAvroModel(restaurantEventPayload);

            kafkaProducer.send(restaurantServiceConfigData.getRestaurantCreatedTopicName(),
                    sagaId,
                    restaurantCreatedAvroModel,
                    kafkaMessageHelper.getKafkaCallback(restaurantServiceConfigData.getRestaurantCreatedTopicName(),
                            restaurantCreatedAvroModel,
                            restaurantOutboxMessage,
                            outboxCallback,
                            restaurantEventPayload.getRestaurantId(),
                            "RestaurantCreatedAvroModel"));

            log.info("RestaurantCreatedAvroModel sent to Kafka for restaurant id: {} and saga id: {}",
                    restaurantCreatedAvroModel.getRestaurantId(), sagaId);
        } catch (Exception e) {
            log.error("Error while sending RestaurantCreatedAvroModel message to kafka with restaurant id: {} and saga id: {}, error: {}",
                    restaurantEventPayload.getRestaurantId(), sagaId, e.getMessage());
        }
    }
}
