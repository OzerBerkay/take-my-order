package com.berkay.restaurant.service.domain.dto.create;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class CreateRestaurantResponse {
    @NotNull
    private final UUID restaurantId;
    @NotNull
    private final String message;
}