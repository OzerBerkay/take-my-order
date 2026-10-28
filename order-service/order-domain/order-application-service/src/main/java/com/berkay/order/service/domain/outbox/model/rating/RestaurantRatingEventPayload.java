package com.berkay.order.service.domain.outbox.model.rating;

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
public class RestaurantRatingEventPayload {

    @JsonProperty
    private String id;

    @JsonProperty
    private String restaurantId;

    @JsonProperty
    private Double averageRating;

    @JsonProperty
    private int totalReviews;

    @JsonProperty
    private ZonedDateTime updatedAt;
}
