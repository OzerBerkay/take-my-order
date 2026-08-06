package com.berkay.restaurant.service.domain.entity;

import com.berkay.domain.entity.BaseEntity;
import com.berkay.domain.valueobject.RestaurantId;
import com.berkay.domain.valueobject.RestaurantId;
import com.berkay.restaurant.service.domain.valueobject.RestaurantPersonnelId;

import java.time.ZonedDateTime;
import java.util.UUID;

public class RestaurantPersonnel extends BaseEntity<RestaurantPersonnelId> {
    private final RestaurantId restaurantId;
    private final UUID userId;
    private final ZonedDateTime createdAt;

    private RestaurantPersonnel(Builder builder) {
        super.setId(builder.restaurantPersonnelId);
        restaurantId = builder.restaurantId;
        userId = builder.userId;
        createdAt = builder.createdAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public RestaurantId getRestaurantId() {
        return restaurantId;
    }

    public UUID getUserId() {
        return userId;
    }

    public ZonedDateTime getCreatedAt() {
        return createdAt;
    }

    public static final class Builder {
        private RestaurantPersonnelId restaurantPersonnelId;
        private RestaurantId restaurantId;
        private UUID userId;
        private ZonedDateTime createdAt;

        private Builder() {
        }

        public Builder restaurantPersonnelId(RestaurantPersonnelId val) {
            restaurantPersonnelId = val;
            return this;
        }

        public Builder restaurantId(RestaurantId val) {
            restaurantId = val;
            return this;
        }

        public Builder userId(UUID val) {
            userId = val;
            return this;
        }

        public Builder createdAt(ZonedDateTime val) {
            createdAt = val;
            return this;
        }

        public RestaurantPersonnel build() {
            return new RestaurantPersonnel(this);
        }
    }
}
