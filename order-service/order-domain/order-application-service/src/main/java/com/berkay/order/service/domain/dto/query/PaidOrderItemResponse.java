package com.berkay.order.service.domain.dto.query;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class PaidOrderItemResponse {
    private final UUID productId;
    private final int quantity;
    private final BigDecimal price;
    private final BigDecimal subTotal;
}
