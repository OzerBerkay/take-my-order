package com.berkay.restaurant.service.domain.dto.update.restaurant;

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
public class UpdateCategoriesCommand {

    @NotNull
    private final UUID restaurantId;

    @NotNull
    private final Long categoryVersion;

    @Valid
    @NotNull
    private final List<CategoryPayload> categories;
}
