package com.berkay.restaurant.service.domain.dto.update.restaurant;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import com.berkay.restaurant.service.domain.valueobject.CuisineType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import java.math.BigDecimal;

import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor

public class UpdateRestaurantCommand {
    @NotNull
    private final UUID restaurantId;

    @Size(min = 2, max = 50, message = "Restaurant name must be between 2 and 50 characters")
    private final String restaurantName;

    private final Boolean active;

    private final Boolean available;
    private final BigDecimal minimumOrderAmount;
    private final BigDecimal deliveryFee;

    private final String street;
    private final String city;
    private final String postalCode;
    private final String phoneNumber;
    private final Integer averageDeliveryTimeInMinutes;
    private final CuisineType cuisineType;
    private final String description;
    private final String logoUrl;

}
