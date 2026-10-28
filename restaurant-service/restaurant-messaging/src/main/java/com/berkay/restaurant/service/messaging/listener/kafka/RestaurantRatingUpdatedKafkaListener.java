package com.berkay.restaurant.service.messaging.listener.kafka;

import com.berkay.kafka.consumer.KafkaConsumer;
import com.berkay.kafka.order.avro.model.RestaurantRatingUpdatedAvroModel;
import com.berkay.restaurant.service.domain.ports.input.message.listener.RestaurantRatingMessageListener;
import com.berkay.restaurant.service.messaging.mapper.RestaurantMessagingDataMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class RestaurantRatingUpdatedKafkaListener implements KafkaConsumer<RestaurantRatingUpdatedAvroModel> {

    private final RestaurantRatingMessageListener restaurantRatingMessageListener;
    private final RestaurantMessagingDataMapper restaurantMessagingDataMapper;

    public RestaurantRatingUpdatedKafkaListener(RestaurantRatingMessageListener restaurantRatingMessageListener,
                                                RestaurantMessagingDataMapper restaurantMessagingDataMapper) {
        this.restaurantRatingMessageListener = restaurantRatingMessageListener;
        this.restaurantMessagingDataMapper = restaurantMessagingDataMapper;
    }

    @Override
    @KafkaListener(id = "${kafka-consumer-config.restaurant-rating-consumer-group-id:restaurant-rating-topic-consumer}",
            topics = "${restaurant-service.restaurant-rating-updated-topic-name:restaurant-rating-updated}")
    public void receive(@Payload List<RestaurantRatingUpdatedAvroModel> messages,
                        @Header(KafkaHeaders.RECEIVED_KEY) List<String> keys,
                        @Header(KafkaHeaders.RECEIVED_PARTITION) List<Integer> partitions,
                        @Header(KafkaHeaders.OFFSET) List<Long> offsets) {
        log.info("{} number of restaurant rating update events received with keys: {}, partitions: {}, offsets: {}",
                messages.size(), keys, partitions, offsets);

        messages.forEach(avroModel -> {
            try {
                log.info("Processing restaurant rating update for restaurantId: {}, averageRating: {}, totalReviews: {}",
                        avroModel.getRestaurantId(), avroModel.getAverageRating(), avroModel.getTotalReviews());
                restaurantRatingMessageListener.updateRating(
                        restaurantMessagingDataMapper.restaurantRatingUpdatedAvroModelToRestaurantRatingUpdateModel(avroModel));
            } catch (Exception e) {
                log.error("Error processing restaurant rating update for restaurantId: {}", avroModel.getRestaurantId(), e);
            }
        });
    }
}
