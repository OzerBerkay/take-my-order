package com.berkay.order.service.dataaccess.outbox.restaurantrating.mapper;

import com.berkay.order.service.dataaccess.outbox.restaurantrating.entity.RestaurantRatingOutboxEntity;
import com.berkay.order.service.domain.outbox.model.rating.RestaurantRatingOutboxMessage;
import org.springframework.stereotype.Component;

@Component
public class RestaurantRatingOutboxDataAccessMapper {

    public RestaurantRatingOutboxEntity restaurantRatingOutboxMessageToRestaurantRatingOutboxEntity(RestaurantRatingOutboxMessage message) {
        if (message == null) {
            return null;
        }
        return RestaurantRatingOutboxEntity.builder()
                .id(message.getId())
                .restaurantId(message.getRestaurantId())
                .createdAt(message.getCreatedAt())
                .processedAt(message.getProcessedAt())
                .type(message.getType())
                .payload(message.getPayload())
                .outboxStatus(message.getOutboxStatus())
                .version(message.getVersion())
                .build();
    }

    public RestaurantRatingOutboxMessage restaurantRatingOutboxEntityToRestaurantRatingOutboxMessage(RestaurantRatingOutboxEntity entity) {
        if (entity == null) {
            return null;
        }
        return RestaurantRatingOutboxMessage.builder()
                .id(entity.getId())
                .restaurantId(entity.getRestaurantId())
                .createdAt(entity.getCreatedAt())
                .processedAt(entity.getProcessedAt())
                .type(entity.getType())
                .payload(entity.getPayload())
                .outboxStatus(entity.getOutboxStatus())
                .version(entity.getVersion())
                .build();
    }
}
