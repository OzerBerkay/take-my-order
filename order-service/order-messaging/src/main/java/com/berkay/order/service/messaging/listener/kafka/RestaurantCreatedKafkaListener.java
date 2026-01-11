package com.berkay.order.service.messaging.listener.kafka;

import com.berkay.kafka.consumer.KafkaConsumer;
import com.berkay.kafka.order.avro.model.RestaurantCreatedAvroModel;
import com.berkay.order.service.domain.dto.message.RestaurantModel;
import com.berkay.order.service.domain.exception.OrderDomainException;
import com.berkay.order.service.domain.ports.input.message.listener.restaurant.RestaurantCreatedMessageListener;
import com.berkay.order.service.messaging.mapper.OrderMessagingDataMapper;
import lombok.extern.slf4j.Slf4j;
import org.postgresql.util.PSQLState;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.sql.SQLException;
import java.util.List;

@Slf4j
@Component
public class RestaurantCreatedKafkaListener implements KafkaConsumer<RestaurantCreatedAvroModel> {

    private final RestaurantCreatedMessageListener restaurantCreatedMessageListener;
    private final OrderMessagingDataMapper orderMessagingDataMapper;

    public RestaurantCreatedKafkaListener(RestaurantCreatedMessageListener restaurantCreatedMessageListener,
                                          OrderMessagingDataMapper orderMessagingDataMapper) {
        this.restaurantCreatedMessageListener = restaurantCreatedMessageListener;
        this.orderMessagingDataMapper = orderMessagingDataMapper;
    }

    @Override
    @KafkaListener(id = "${kafka-consumer-config.restaurant-created-consumer-group-id}",
            topics = "${order-service.restaurant-created-topic-name}")
    public void receive(@Payload List<RestaurantCreatedAvroModel> messages,
                        @Header(KafkaHeaders.RECEIVED_KEY) List<String> keys,
                        @Header(KafkaHeaders.RECEIVED_PARTITION) List<Integer> partitions,
                        @Header(KafkaHeaders.OFFSET) List<Long> offsets) {

        log.info("{} number of restaurant created messages received with keys {}, partitions {} and offsets {}",
                messages.size(),
                keys.toString(),
                partitions.toString(),
                offsets.toString());

        messages.forEach(restaurantCreatedAvroModel -> {
            try {
                log.info("Processing RestaurantCreatedAvroModel for restaurant id: {}",
                        restaurantCreatedAvroModel.getRestaurantId());

                RestaurantModel restaurantModel = orderMessagingDataMapper
                        .restaurantCreatedAvroModelToRestaurantModel(restaurantCreatedAvroModel);

                restaurantCreatedMessageListener.restaurantCreated(restaurantModel);

            } catch (OptimisticLockingFailureException e) {
                // UPDATE senaryoları için koruma
                // NO-OP: Başka bir thread zaten güncelledi.
                log.error("Caught optimistic locking exception in RestaurantCreatedKafkaListener for restaurant id: {}",
                        restaurantCreatedAvroModel.getRestaurantId());
            } catch (DataAccessException e) {
                // Kök sebebi bul (SQL Hatası mı?)
                SQLException sqlException = (SQLException) e.getRootCause();

                // Eğer hata "Unique Violation" (Zaten kayıtlı) ise:
                if (sqlException != null && sqlException.getSQLState() != null &&
                        PSQLState.UNIQUE_VIOLATION.getState().equals(sqlException.getSQLState())) {

                    // NO-OP (İşlem yapma/yut): Zaten kaydedilmiş, sorun yok.
                    log.error("Caught unique constraint exception with sql state: {} " +
                                    "in RestaurantCreatedKafkaListener for restaurant id: {}",
                            sqlException.getSQLState(), restaurantCreatedAvroModel.getRestaurantId());
                } else {
                    // Diğer DB hataları için hatayı fırlat (Kafka tekrar denesin)
                    throw new OrderDomainException("Throwing DataAccessException in " +
                            "RestaurantCreatedKafkaListener: " + e.getMessage(), e);
                }
            } catch (Exception e) {
                // Beklenmeyen diğer hatalar için de fırlat
                throw new OrderDomainException("Throwing Exception in " +
                        "RestaurantCreatedKafkaListener: " + e.getMessage(), e);
            }
        });
    }
}
