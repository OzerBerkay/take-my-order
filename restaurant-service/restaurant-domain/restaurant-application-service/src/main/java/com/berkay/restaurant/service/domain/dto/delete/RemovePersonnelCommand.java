package com.berkay.restaurant.service.domain.dto.delete;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class RemovePersonnelCommand {

    @NotNull
    private final UUID restaurantId;

    @NotNull
    private final UUID userId;

    @NotNull
    private final UUID removedByMerchantId;
}
