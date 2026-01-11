package com.berkay.restaurant.service.domain.outbox.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;


@Getter
@Builder
@AllArgsConstructor
public class RestaurantEventPayload {
    @JsonProperty
    private String restaurantId;
    @JsonProperty
    private boolean active;
    @JsonProperty
    private List<ProductPayload> products;
    @JsonProperty
    private ZonedDateTime createdAt;

    @Getter
    @Builder
    @AllArgsConstructor
    public static class ProductPayload {
        @JsonProperty
        private String productId;
        @JsonProperty
        private String name;
        @JsonProperty
        private BigDecimal price;
        @JsonProperty
        private boolean available;
    }
}
