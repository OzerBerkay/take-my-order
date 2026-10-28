package com.berkay.order.service.domain.dto.restaurant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RestaurantRatingCalculationResult {
    private final long totalReviews;
    private final Double averageRating;
}
