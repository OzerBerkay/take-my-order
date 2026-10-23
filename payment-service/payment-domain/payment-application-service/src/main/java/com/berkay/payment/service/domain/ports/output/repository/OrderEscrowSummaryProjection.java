package com.berkay.payment.service.domain.ports.output.repository;

import java.math.BigDecimal;

public interface OrderEscrowSummaryProjection {
    BigDecimal getDailySettledRevenue();
    BigDecimal getPendingPayout();
    BigDecimal getDailyRefundedAmount();
}
