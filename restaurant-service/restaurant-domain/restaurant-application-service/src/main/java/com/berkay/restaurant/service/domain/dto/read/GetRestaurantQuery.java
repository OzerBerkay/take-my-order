package com.berkay.restaurant.service.domain.dto.read;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class GetRestaurantQuery {
    @NotNull(message = "Restaurant id cannot be null!")
    private final UUID restaurantId;
}
