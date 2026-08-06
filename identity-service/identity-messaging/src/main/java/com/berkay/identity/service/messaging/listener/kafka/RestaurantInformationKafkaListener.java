package com.berkay.identity.service.messaging.listener.kafka;

import com.berkay.kafka.consumer.KafkaConsumer;
import com.berkay.kafka.order.avro.model.RestaurantInformationAvroModel;
import com.berkay.identity.service.domain.dto.message.RestaurantInformationEventPayload;
import com.berkay.identity.service.ports.input.message.listener.restaurant.RestaurantInformationMessageListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class RestaurantInformationKafkaListener implements KafkaConsumer<RestaurantInformationAvroModel> {

    private final RestaurantInformationMessageListener restaurantInformationMessageListener;

    @Override
    @KafkaListener(id = "${kafka-consumer-config.restaurant-info-consumer-group-id}",
            topics = "${identity-service.restaurant-info-topic-name}")
    public void receive(@Payload List<RestaurantInformationAvroModel> messages,
                        @Header(KafkaHeaders.RECEIVED_KEY) List<String> keys,
                        @Header(KafkaHeaders.RECEIVED_PARTITION) List<Integer> partitions,
                        @Header(KafkaHeaders.OFFSET) List<Long> offsets) {

        log.info("{} number of restaurant information messages received with keys {}, partitions {} and offsets {}",
                messages.size(), keys.toString(), partitions.toString(), offsets.toString());

        messages.forEach(avroModel -> {
            String eventType = avroModel.getEventType() != null ? avroModel.getEventType() : "";

            switch (eventType) {
                case "RESTAURANT_CREATED":
                    RestaurantInformationEventPayload payload = RestaurantInformationEventPayload.builder()
                            .restaurantId(avroModel.getRestaurantId())
                            .merchantId(avroModel.getMerchantId())
                            .name(avroModel.getName())
                            .active(avroModel.getActive())
                            .createdAt(java.time.ZonedDateTime.ofInstant(avroModel.getCreatedAt(), ZoneId.of("UTC")))
                            .build();

                    restaurantInformationMessageListener.restaurantCreated(payload);
                    break;
                case "RESTAURANT_UPDATED":
                    // Identity service currently only cares about creation to assign roles.
                    log.debug("Received RESTAURANT_UPDATED event for restaurant {}. Ignored in Identity Service.", avroModel.getRestaurantId());
                    break;
                default:
                    // Fallback for old messages or unknown types
                    if (avroModel.getMerchantId() != null) {
                        RestaurantInformationEventPayload fallbackPayload = RestaurantInformationEventPayload.builder()
                                .restaurantId(avroModel.getRestaurantId())
                                .merchantId(avroModel.getMerchantId())
                                .name(avroModel.getName())
                                .active(avroModel.getActive())
                                .createdAt(java.time.ZonedDateTime.ofInstant(avroModel.getCreatedAt(), ZoneId.of("UTC")))
                                .build();
                        restaurantInformationMessageListener.restaurantCreated(fallbackPayload);
                    } else {
                        log.warn("Received RestaurantInformationAvroModel with unknown eventType '{}' and no merchantId for restaurant {}. Ignored.", eventType, avroModel.getRestaurantId());
                    }
                    break;
            }
        });
    }
}
