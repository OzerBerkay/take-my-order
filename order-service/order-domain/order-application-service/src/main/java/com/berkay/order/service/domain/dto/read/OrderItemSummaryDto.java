package com.berkay.order.service.domain.dto.read;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class OrderItemSummaryDto {
    private final String name;
    private final int quantity;
}
