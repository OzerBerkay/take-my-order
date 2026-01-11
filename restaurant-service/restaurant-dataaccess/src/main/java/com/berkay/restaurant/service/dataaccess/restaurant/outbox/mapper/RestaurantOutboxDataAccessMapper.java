package com.berkay.restaurant.service.dataaccess.restaurant.outbox.mapper;

import com.berkay.restaurant.service.dataaccess.restaurant.outbox.entity.RestaurantOutboxEntity;
import com.berkay.restaurant.service.domain.outbox.model.RestaurantOutboxMessage;
import org.springframework.stereotype.Component;

@Component
public class RestaurantOutboxDataAccessMapper {

    public RestaurantOutboxEntity orderOutboxMessageToOutboxEntity(RestaurantOutboxMessage restaurantOutboxMessage) {
        return RestaurantOutboxEntity.builder()
                .id(restaurantOutboxMessage.getId())
                .sagaId(restaurantOutboxMessage.getSagaId())
                .createdAt(restaurantOutboxMessage.getCreatedAt())
                .type(restaurantOutboxMessage.getType())
                .payload(restaurantOutboxMessage.getPayload())
                .outboxStatus(restaurantOutboxMessage.getOutboxStatus())
                .version(restaurantOutboxMessage.getVersion())
                .build();
    }

    public RestaurantOutboxMessage outboxEntityToOrderOutboxMessage(RestaurantOutboxEntity restaurantOutboxEntity) {
        return RestaurantOutboxMessage.builder()
                .id(restaurantOutboxEntity.getId())
                .sagaId(restaurantOutboxEntity.getSagaId())
                .createdAt(restaurantOutboxEntity.getCreatedAt())
                .type(restaurantOutboxEntity.getType())
                .payload(restaurantOutboxEntity.getPayload())
                .outboxStatus(restaurantOutboxEntity.getOutboxStatus())
                .version(restaurantOutboxEntity.getVersion())
                .build();
    }
}
