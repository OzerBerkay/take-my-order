package com.berkay.payment.service.domain.dto.wallet;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WalletDepositCommand {
    private UUID ownerId;
    @NotNull(message = "Deposit amount is required")
    @Positive(message = "Deposit amount must be greater than zero")
    @DecimalMax(value = "99999.99", message = "Deposit amount cannot exceed 99,999.99")
    private BigDecimal amount;
    private String idempotencyKey;
}
