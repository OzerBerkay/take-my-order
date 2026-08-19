package com.berkay.restaurant.service.domain.dto.read;

import com.berkay.restaurant.service.domain.valueobject.CuisineType;
import com.berkay.restaurant.service.domain.valueobject.Address;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class RestaurantModel {
    private final UUID restaurantId;
    private final String name;
    private final String description;
    private final String logoUrl;
    private final CuisineType cuisineType;
    private final Integer averageDeliveryTimeInMinutes;
    private final BigDecimal deliveryFee;
    private final BigDecimal minimumOrderAmount;
    private final Address address;
    private final Boolean active;
    private final Boolean available;
}
