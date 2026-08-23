package com.berkay.order.service.domain.dto.read;

import com.berkay.domain.valueobject.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class OrderSummary {
    private final UUID orderTrackingId;
    private final UUID restaurantId;
    private final OrderStatus orderStatus;
    private final BigDecimal totalAmount;
    private final String failureMessages;
    private final java.time.ZonedDateTime createdAt;
}
