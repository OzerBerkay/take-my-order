package com.berkay.restaurant.service.messaging.publisher.kafka;

import com.berkay.kafka.order.avro.model.RestaurantInformationAvroModel;
import com.berkay.kafka.producer.KafkaMessageHelper;
import com.berkay.kafka.producer.service.KafkaProducer;
import com.berkay.outbox.OutboxStatus;
import com.berkay.restaurant.service.domain.config.RestaurantServiceConfigData;
import com.berkay.restaurant.service.domain.outbox.model.RestaurantEventPayload;
import com.berkay.restaurant.service.domain.outbox.model.RestaurantOutboxMessage;
import com.berkay.restaurant.service.domain.ports.output.RestaurantInformationMessagePublisher;
import com.berkay.restaurant.service.messaging.mapper.RestaurantMessagingDataMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.function.BiConsumer;

@Slf4j
@Component
public class RestaurantInformationEventKafkaPublisher implements RestaurantInformationMessagePublisher {

    private final RestaurantMessagingDataMapper restaurantMessagingDataMapper;
    private final KafkaProducer<String, RestaurantInformationAvroModel> kafkaProducer;
    private final RestaurantServiceConfigData restaurantServiceConfigData;
    private final KafkaMessageHelper kafkaMessageHelper;

    public RestaurantInformationEventKafkaPublisher(RestaurantMessagingDataMapper restaurantMessagingDataMapper,
                                                    KafkaProducer<String, RestaurantInformationAvroModel> kafkaProducer,
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

        String outboxId = restaurantOutboxMessage.getId().toString();
        String partitionKey = restaurantEventPayload.getRestaurantId();

        log.info("Received RestaurantOutboxMessage for restaurant id: {} and outbox id: {}",
                restaurantEventPayload.getRestaurantId(),
                outboxId);

        try {
            RestaurantInformationAvroModel restaurantInformationAvroModel =
                    restaurantMessagingDataMapper.restaurantEventPayloadToRestaurantInformationAvroModel(restaurantEventPayload);

            kafkaProducer.send(restaurantServiceConfigData.getRestaurantInformationTopicName(),
                    partitionKey,
                    restaurantInformationAvroModel,
                    kafkaMessageHelper.getKafkaCallback(restaurantServiceConfigData.getRestaurantInformationTopicName(),
                            restaurantInformationAvroModel,
                            restaurantOutboxMessage,
                            outboxCallback,
                            restaurantEventPayload.getRestaurantId(),
                            "RestaurantInformationAvroModel"));

            log.info("RestaurantInformationAvroModel sent to Kafka for restaurant id: {} and saga id: {}",
                    restaurantInformationAvroModel.getRestaurantId(), outboxId);
        } catch (Exception e) {
            log.error("Error while sending RestaurantInformationAvroModel message to kafka with restaurant id: {} and saga id: {}, error: {}",
                    restaurantEventPayload.getRestaurantId(), outboxId, e.getMessage());
        }
    }
}
