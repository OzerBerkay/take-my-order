package com.berkay.order.service.domain.dto.operation;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StartDeliveryOrderCommand {
    @NotNull
    private UUID orderId;
}
