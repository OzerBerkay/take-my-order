package com.berkay.restaurant.service.domain.dto.read;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class GetPublicRestaurantQueryResponse {
    private final RestaurantModel restaurant;
}
