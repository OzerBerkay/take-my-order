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

    @Size(max = 50, message = "City cannot exceed 50 characters")
    private String city;
    @Size(max = 50, message = "District cannot exceed 50 characters")
    private String district;
    @Size(max = 50, message = "Neighborhood cannot exceed 50 characters")
    private String neighborhood;
    @Size(max = 50, message = "Street cannot exceed 50 characters")
    private String street;
    @Size(max = 10, message = "Building number cannot exceed 10 characters")
    private String buildingNumber;
    @Size(max = 10, message = "Door number cannot exceed 10 characters")
    private String doorNumber;
    
    private String phoneNumber;
    private Integer averageDeliveryTimeInMinutes;
    private java.util.List<java.util.UUID> cuisineIds;
    private String description;
    private String logoUrl;
    private String bannerUrl;

}
