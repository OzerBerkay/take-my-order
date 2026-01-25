package com.berkay.restaurant.service.domain.dto.create.restaurant;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class CreateRestaurantCommand {
    @NotNull(message = "Restaurant name cannot be null!")
    @Size(min = 2, max = 50, message = "Restaurant name must be between 2 and 50 characters!")
    private final String restaurantName;

    @NotNull
    private final boolean active; // Default true olabilir

    @Valid // İçindeki objeleri de validate etmesi için
    private final List<CreateProductCommand> products;
}