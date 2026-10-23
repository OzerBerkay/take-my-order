package com.berkay.order.service.domain.dto.read;

import com.berkay.domain.valueobject.CancelReason;
import com.berkay.domain.valueobject.OrderStatus;
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
public class OrderSummary {
    private final UUID orderId;
    private final UUID orderTrackingId;
    private final UUID restaurantId;
    private final String restaurantName;
    private final OrderStatus orderStatus;
    private final BigDecimal totalAmount;
    private final int totalItemCount;
    private final List<OrderItemSummaryDto> items;
    private final String failureMessages;
    private final CancelReason cancelReason;
    private final ZonedDateTime createdAt;
}
