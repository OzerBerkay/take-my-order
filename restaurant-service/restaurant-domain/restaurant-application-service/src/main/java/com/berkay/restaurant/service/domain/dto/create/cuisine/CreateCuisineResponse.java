package com.berkay.restaurant.service.domain.dto.create.cuisine;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class CreateCuisineResponse {
    private final UUID cuisineId;
    private final String message;
}
