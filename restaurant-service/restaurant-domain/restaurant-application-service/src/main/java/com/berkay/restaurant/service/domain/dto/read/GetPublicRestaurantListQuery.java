package com.berkay.restaurant.service.domain.dto.read;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class GetPublicRestaurantListQuery {
    private final String searchName;
    private final List<String> cuisineCodes;
    private final Boolean available;
    private final java.math.BigDecimal maxMinimumOrderAmount;
    private final Integer maxDeliveryTime;
    private final java.math.BigDecimal minRating;
    private final String sortBy;
    private final int page;
    private final int size;

    public GetPublicRestaurantListQuery(String searchName, List<String> cuisineCodes, Boolean available, java.math.BigDecimal maxMinimumOrderAmount, Integer maxDeliveryTime, int page, int size) {
        this(searchName, cuisineCodes, available, maxMinimumOrderAmount, maxDeliveryTime, null, null, page, size);
    }
}
