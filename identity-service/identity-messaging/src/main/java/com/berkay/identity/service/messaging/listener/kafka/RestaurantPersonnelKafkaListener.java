package com.berkay.identity.service.messaging.listener.kafka;

import com.berkay.kafka.consumer.KafkaConsumer;
import com.berkay.kafka.order.avro.model.RestaurantPersonnelAvroModel;
import com.berkay.identity.service.ports.input.message.listener.restaurant.RestaurantPersonnelMessageListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class RestaurantPersonnelKafkaListener implements KafkaConsumer<RestaurantPersonnelAvroModel> {

    private final RestaurantPersonnelMessageListener restaurantPersonnelMessageListener;

    @Override
    @KafkaListener(id = "${kafka-consumer-config.restaurant-personnel-consumer-group-id}",
            topics = "${identity-service.restaurant-personnel-topic-name}")
    public void receive(@Payload List<RestaurantPersonnelAvroModel> messages,
                        @Header(KafkaHeaders.RECEIVED_KEY) List<String> keys,
                        @Header(KafkaHeaders.RECEIVED_PARTITION) List<Integer> partitions,
                        @Header(KafkaHeaders.OFFSET) List<Long> offsets) {

        log.info("{} number of restaurant personnel messages received with keys {}, partitions {} and offsets {}",
                messages.size(), keys.toString(), partitions.toString(), offsets.toString());

        messages.forEach(avroModel -> {
            try {
                String eventType = avroModel.getEventType() != null ? avroModel.getEventType() : "";

                switch (eventType) {
                    case "PERSONNEL_ADDED":
                        restaurantPersonnelMessageListener.personnelAdded(avroModel);
                        break;
                    case "PERSONNEL_REMOVED":
                        restaurantPersonnelMessageListener.personnelRemoved(avroModel);
                        break;
                    default:
                        // Fallback
                        log.warn("Unknown eventType {} for RestaurantPersonnelAvroModel. Falling back to personnelAdded.", eventType);
                        restaurantPersonnelMessageListener.personnelAdded(avroModel);
                        break;
                }
            } catch (Exception e) {
                log.error("Failed to process RestaurantPersonnelAvroModel for user {} in restaurant {}. Error: {}",
                        avroModel.getUserId(), avroModel.getRestaurantId(), e.getMessage());
            }
        });
    }
}
