package com.berkay.restaurant.service.dataaccess.restaurant.outbox.mapper;

import com.berkay.restaurant.service.dataaccess.restaurant.outbox.entity.RestaurantOutboxEntity;
import com.berkay.restaurant.service.domain.outbox.model.RestaurantOutboxMessage;
import org.springframework.stereotype.Component;

@Component
public class RestaurantOutboxDataAccessMapper {

    public RestaurantOutboxEntity restaurantOutboxMessageToRestaurantOutboxEntity(RestaurantOutboxMessage restaurantOutboxMessage) {
        return RestaurantOutboxEntity.builder()
                .id(restaurantOutboxMessage.getId())
                .createdAt(restaurantOutboxMessage.getCreatedAt())
                .type(restaurantOutboxMessage.getType())
                .payload(restaurantOutboxMessage.getPayload())
                .outboxStatus(restaurantOutboxMessage.getOutboxStatus())
                .version(restaurantOutboxMessage.getVersion())
                .build();
    }

    public RestaurantOutboxMessage restaurantOutboxEntityToRestaurantOutboxMessage(RestaurantOutboxEntity restaurantOutboxEntity) {
        return RestaurantOutboxMessage.builder()
                .id(restaurantOutboxEntity.getId())
                .createdAt(restaurantOutboxEntity.getCreatedAt())
                .type(restaurantOutboxEntity.getType())
                .payload(restaurantOutboxEntity.getPayload())
                .outboxStatus(restaurantOutboxEntity.getOutboxStatus())
                .version(restaurantOutboxEntity.getVersion())
                .build();
    }
}
