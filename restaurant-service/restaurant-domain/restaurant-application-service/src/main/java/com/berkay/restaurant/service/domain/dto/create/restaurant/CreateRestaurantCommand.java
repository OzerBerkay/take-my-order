package com.berkay.restaurant.service.domain.dto.create.restaurant;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
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

    @NotNull
    private final String street;
    @NotNull
    private final String city;
    @NotNull
    private final String postalCode;
    
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

    @Valid // İçindeki objeleri de validate etmesi için
    private final List<CreateProductCommand> products;

    public void setMerchantId(String merchantId) {
        this.merchantId = merchantId;
    }
}