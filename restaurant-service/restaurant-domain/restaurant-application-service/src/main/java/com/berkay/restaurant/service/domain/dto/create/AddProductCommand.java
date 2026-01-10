package com.berkay.restaurant.service.domain.dto.create;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class AddProductCommand {
    @NotNull
    private final UUID restaurantId;
    @NotNull
    @Size(min = 2, max = 50)
    private final String name;
    @NotNull
    @DecimalMin("0.01")
    private final BigDecimal price;
    @NotNull
    private final int stock;
    @NotNull
    private final boolean available;
}