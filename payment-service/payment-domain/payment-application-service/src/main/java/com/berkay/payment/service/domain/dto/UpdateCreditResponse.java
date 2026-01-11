package com.berkay.payment.service.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class UpdateCreditResponse {
    private final UUID customerId;
    private final BigDecimal newBalance;
}
