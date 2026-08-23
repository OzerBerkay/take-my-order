package com.berkay.order.service.domain.dto.read;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class GetOrdersQuery {
    @NotNull
    private final UUID customerId;
    private final int page;
    private final int size;
}
