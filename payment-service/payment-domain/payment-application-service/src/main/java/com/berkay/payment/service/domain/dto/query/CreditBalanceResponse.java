package com.berkay.payment.service.domain.dto.query;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class CreditBalanceResponse {
    private final UUID customerId;
    private final BigDecimal currentBalance;
}
