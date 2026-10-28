package com.berkay.order.service.domain.dto.review;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetRestaurantReviewsQuery {
    @NotNull
    private UUID restaurantId;
    private String status;
    private int page;
    private int size;
}
