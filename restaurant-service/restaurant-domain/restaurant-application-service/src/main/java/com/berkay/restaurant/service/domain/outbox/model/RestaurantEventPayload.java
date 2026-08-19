package com.berkay.restaurant.service.domain.outbox.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;


import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantEventPayload {
    @JsonProperty
    private String restaurantId;
    @JsonProperty
    private String merchantId;
    @JsonProperty
    private String name;
    @JsonProperty
    private boolean active;
    @JsonProperty
    private boolean available;
    @JsonProperty
    private List<ProductPayload> products;
    @JsonProperty
    private ZonedDateTime createdAt;
    @JsonProperty
    private String eventType;

    @Getter
    @Builder
    @NoArgsConstructor
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
