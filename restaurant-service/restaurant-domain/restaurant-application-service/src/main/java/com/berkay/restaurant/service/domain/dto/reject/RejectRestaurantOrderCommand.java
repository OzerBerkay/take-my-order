package com.berkay.restaurant.service.domain.dto.reject;

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
@NoArgsConstructor
public class RejectRestaurantOrderCommand {
    @NotNull
    private UUID restaurantId;
    @NotNull
    private UUID orderId;
    @NotNull
    private List<String> failureMessages;
}
