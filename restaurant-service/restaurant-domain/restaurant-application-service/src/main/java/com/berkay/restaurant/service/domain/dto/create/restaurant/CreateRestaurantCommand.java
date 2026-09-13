package com.berkay.restaurant.service.domain.dto.create.restaurant;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Positive;

@Getter
@Builder
@AllArgsConstructor
public class CreateRestaurantCommand {
    @NotNull(message = "Restaurant name cannot be null!")
    @Size(min = 2, max = 50, message = "Restaurant name must be between 2 and 50 characters!")
    private final String restaurantName;

    // Set by controller from JWT
    private String merchantId;

    @NotNull
    private final boolean active; // Default true olabilir

    @NotBlank(message = "City cannot be blank")
    @Size(max = 50, message = "City cannot exceed 50 characters")
    private final String city;
    
    @NotBlank(message = "District cannot be blank")
    @Size(max = 50, message = "District cannot exceed 50 characters")
    private final String district;
    
    @NotBlank(message = "Neighborhood cannot be blank")
    @Size(max = 50, message = "Neighborhood cannot exceed 50 characters")
    private final String neighborhood;
    
    @NotBlank(message = "Street cannot be blank")
    @Size(max = 50, message = "Street cannot exceed 50 characters")
    private final String street;
    
    @NotBlank(message = "Building number cannot be blank")
    @Size(max = 10, message = "Building number cannot exceed 10 characters")
    private final String buildingNumber;
    
    @NotBlank(message = "Door number cannot be blank")
    @Size(max = 10, message = "Door number cannot exceed 10 characters")
    private final String doorNumber;
    
    @NotNull
    private final String phoneNumber;
    
    @PositiveOrZero
    private final BigDecimal minimumOrderAmount;
    
    @PositiveOrZero
    private final BigDecimal deliveryFee;
    
    @PositiveOrZero
    private final Integer averageDeliveryTimeInMinutes;
    
    private final List<UUID> cuisineIds;
    
    private final String description;
    
    private final String logoUrl;
    
    private final String bannerUrl;

    public void setMerchantId(String merchantId) {
        this.merchantId = merchantId;
    }
}