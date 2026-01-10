package com.berkay.restaurant.service.domain.dto.create;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class CreateRestaurantCommand {
    @NotNull(message = "Restaurant name cannot be null!")
    @Size(min = 2, max = 50, message = "Restaurant name must be between 2 and 50 characters!")
    private final String restaurantName;
}