package com.berkay.order.service.domain.dto.query;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class PaidOrderResponse {
    private final UUID orderId;
    private final UUID trackingId;
    private final String orderStatus;
    private final BigDecimal price;
    private final ZonedDateTime createdAt;
    private final List<PaidOrderItemResponse> items;
}
