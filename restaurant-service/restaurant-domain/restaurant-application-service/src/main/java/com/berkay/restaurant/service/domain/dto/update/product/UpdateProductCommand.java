package com.berkay.restaurant.service.domain.dto.update.product;

import com.berkay.restaurant.service.domain.dto.base.BaseProductCommand;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Getter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor(force = true)
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class UpdateProductCommand extends BaseProductCommand {

    @NotNull(message = "Restaurant ID cannot be null!")
    private final UUID restaurantId;

    @NotNull(message = "Product ID cannot be null!")
    private final UUID productId;
}
