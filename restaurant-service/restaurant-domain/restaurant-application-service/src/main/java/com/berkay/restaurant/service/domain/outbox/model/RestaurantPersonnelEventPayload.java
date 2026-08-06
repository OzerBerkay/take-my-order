package com.berkay.restaurant.service.domain.outbox.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantPersonnelEventPayload {
    @JsonProperty
    private String restaurantId;
    @JsonProperty
    private String userId;
    @JsonProperty
    private String addedByMerchantId;
    @JsonProperty
    private ZonedDateTime createdAt;
    @JsonProperty
    private String eventType;
}
