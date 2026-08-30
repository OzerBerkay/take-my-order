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
    private final int page;
    private final int size;
}
