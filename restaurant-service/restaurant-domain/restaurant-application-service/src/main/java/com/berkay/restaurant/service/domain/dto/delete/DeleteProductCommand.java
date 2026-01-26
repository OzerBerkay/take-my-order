package com.berkay.restaurant.service.domain.dto.delete;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class DeleteProductCommand {

    @NotNull(message = "Restaurant id cannot be null!")
    private final UUID restaurantId;

    @NotNull(message = "Product id cannot be null!")
    private final UUID productId;
}
