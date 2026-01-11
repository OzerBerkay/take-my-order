package com.berkay.payment.service.domain.dto;

import com.berkay.payment.service.domain.valueobject.TransactionType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class UpdateCreditCommand {
    @NotNull
    private final UUID customerId;
    @NotNull
    private final BigDecimal amount;
    @NotNull
    private final TransactionType transactionType; // DEBIT (Çek) veya CREDIT (Yükle)
}