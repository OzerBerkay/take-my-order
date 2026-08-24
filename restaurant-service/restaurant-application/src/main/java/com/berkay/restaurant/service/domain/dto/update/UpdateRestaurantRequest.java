package com.berkay.restaurant.service.domain.dto.update;

import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Min;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import java.math.BigDecimal;

@Getter
@Builder
@AllArgsConstructor

public class UpdateRestaurantRequest {
    @Size(min = 2, max = 50, message = "Restaurant name must be between 2 and 50 characters")
    private String restaurantName;

    private Boolean active;

    private Boolean available;
    @Min(value = 0, message = "Minimum order amount cannot be negative")
    private BigDecimal minimumOrderAmount;
    @Min(value = 0, message = "Delivery fee cannot be negative")
    private BigDecimal deliveryFee;

    private String street;
    private String city;
    private String postalCode;
    private String phoneNumber;
    private Integer averageDeliveryTimeInMinutes;
    private java.util.List<java.util.UUID> cuisineIds;
    private String description;
    private String logoUrl;
    private String bannerUrl;

}
