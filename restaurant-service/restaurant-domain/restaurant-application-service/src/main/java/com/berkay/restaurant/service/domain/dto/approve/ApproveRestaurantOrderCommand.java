package com.berkay.restaurant.service.domain.dto.approve;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ApproveRestaurantOrderCommand {
    @NotNull
    private UUID restaurantId;
    @NotNull
    private UUID orderId;
}
