package com.berkay.order.service.messaging.listener.kafka;

import com.berkay.kafka.consumer.KafkaConsumer;
import com.berkay.kafka.order.avro.model.RestaurantInformationAvroModel;
import com.berkay.order.service.domain.dto.message.RestaurantModel;
import com.berkay.order.service.domain.exception.OrderDomainException;
import com.berkay.order.service.domain.ports.input.message.listener.restaurant.RestaurantInformationMessageListener;
import com.berkay.order.service.messaging.mapper.OrderMessagingDataMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class RestaurantInformationKafkaListener implements KafkaConsumer<RestaurantInformationAvroModel> {

    private final RestaurantInformationMessageListener restaurantInformationMessageListener;
    private final OrderMessagingDataMapper orderMessagingDataMapper;

    public RestaurantInformationKafkaListener(RestaurantInformationMessageListener restaurantInformationMessageListener,
                                              OrderMessagingDataMapper orderMessagingDataMapper) {
        this.restaurantInformationMessageListener = restaurantInformationMessageListener;
        this.orderMessagingDataMapper = orderMessagingDataMapper;
    }

    @Override
    @KafkaListener(id = "${kafka-consumer-config.restaurant-information-consumer-group-id}",
            topics = "${order-service.restaurant-information-topic-name}")
    public void receive(@Payload List<RestaurantInformationAvroModel> messages,
                        @Header(KafkaHeaders.RECEIVED_KEY) List<String> keys,
                        @Header(KafkaHeaders.RECEIVED_PARTITION) List<Integer> partitions,
                        @Header(KafkaHeaders.OFFSET) List<Long> offsets) {

        log.info("{} number of restaurant information messages received with keys {}, partitions {} and offsets {}",
                messages.size(),
                keys.toString(),
                partitions.toString(),
                offsets.toString());

        messages.forEach(restaurantInformationAvroModel -> {
            try {
                log.info("Processing Restaurant Model for restaurant id: {}",
                        restaurantInformationAvroModel.getRestaurantId());

                RestaurantModel restaurantModel = orderMessagingDataMapper
                        .restaurantInformationAvroModelToRestaurantModel(restaurantInformationAvroModel);

                restaurantInformationMessageListener.restaurantInformationReceived(restaurantModel);

                log.info("Restaurant Model processed successfully for restaurant id: {}",
                        restaurantInformationAvroModel.getRestaurantId());

            } catch (OptimisticLockingFailureException e) {
                // UPDATE senaryoları için koruma
                // NO-OP: Başka bir thread zaten güncelledi.
                log.error("Caught optimistic locking exception in RestaurantInformationKafkaListener for restaurant id: {}",
                        restaurantInformationAvroModel.getRestaurantId());
            } catch (DataAccessException e) {
                log.error("Database access exception for restaurant id: {}. Message: {}",
                        restaurantInformationAvroModel.getRestaurantId(), e.getMessage());
                // DB hataları için hatayı fırlat (Kafka tekrar denesin)
                throw new OrderDomainException("Throwing DataAccessException in " +
                        "RestaurantInformationKafkaListener: " + e.getMessage(), e);

            } catch (Exception e) {
                log.error("Unexpected exception for restaurant id: {}. Message: {}",
                        restaurantInformationAvroModel.getRestaurantId(), e.getMessage());
                // Beklenmeyen diğer hatalar için de fırlat
                throw new OrderDomainException("Throwing Exception in " +
                        "RestaurantInformationKafkaListener: " + e.getMessage(), e);
            }
        });
    }
}
