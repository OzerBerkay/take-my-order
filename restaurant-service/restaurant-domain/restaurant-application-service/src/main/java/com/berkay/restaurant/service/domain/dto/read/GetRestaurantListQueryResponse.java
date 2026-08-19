package com.berkay.restaurant.service.domain.dto.read;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class GetRestaurantListQueryResponse {
    private final List<RestaurantModel> restaurants;
}
