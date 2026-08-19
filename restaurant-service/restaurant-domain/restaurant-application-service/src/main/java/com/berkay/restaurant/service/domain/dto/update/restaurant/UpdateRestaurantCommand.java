package com.berkay.restaurant.service.domain.dto.update.restaurant;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

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
}
