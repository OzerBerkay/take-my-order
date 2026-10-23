package com.berkay.payment.service.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Builder
@AllArgsConstructor
public class OrderDeliveredRequest {
    private String id;
    private String orderId;
    private String restaurantId;
    private String customerId;
    private BigDecimal price;
    private Instant createdAt;
}
