package com.berkay.payment.service.domain.dto.create;

import com.berkay.payment.service.domain.valueobject.TransactionType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreditOperationRequest {

    @NotNull(message = "Amount cannot be null!")
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero!")
    @Digits(integer = 10, fraction = 2, message = "Amount cannot have more than 2 decimal places!")
    private BigDecimal amount;

    @NotNull(message = "Transaction type is required! (DEBIT or CREDIT)")
    private TransactionType transactionType;
}
