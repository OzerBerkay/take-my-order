package com.berkay.restaurant.service.domain.dto.read;

import com.berkay.restaurant.service.domain.valueobject.CuisineType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class GetPublicRestaurantListQuery {
    private final String searchName;
    private final CuisineType cuisineType;
    private final Boolean available;
    private final int page;
    private final int size;
}
