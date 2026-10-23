package com.berkay.payment.service.domain.dto.wallet;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class RestaurantFinancialSummaryResponse {
    private final UUID restaurantId;
    private final BigDecimal withdrawableBalance;
    private final BigDecimal dailySettledRevenue;
    private final BigDecimal pendingPayout;
    private final BigDecimal dailyRefundedAmount;
}
