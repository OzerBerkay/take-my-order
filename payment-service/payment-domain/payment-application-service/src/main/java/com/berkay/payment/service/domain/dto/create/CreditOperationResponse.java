package com.berkay.payment.service.domain.dto.create;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class CreditOperationResponse {
    private final UUID customerId;
    private final BigDecimal newBalance;
}
