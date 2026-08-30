package com.berkay.restaurant.service.domain.dto.create.product;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(force = true)
public class AddProductBatchCommand {
    @NotNull(message = "Restaurant id cannot be null!")
    private final UUID restaurantId;

    @Valid
    @NotNull(message = "Products list cannot be null!")
    private final List<com.berkay.restaurant.service.domain.dto.create.restaurant.CreateProductCommand> products;
}
