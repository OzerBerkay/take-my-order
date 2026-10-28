package com.berkay.restaurant.service.domain.dto.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
@AllArgsConstructor
public class RestaurantRatingUpdateModel {
    private final String id;
    private final String restaurantId;
    private final Double averageRating;
    private final int totalReviews;
    private final Instant updatedAt;
}
