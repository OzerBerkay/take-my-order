package com.berkay.payment.service.domain.dto.wallet;

import com.berkay.payment.service.domain.valueobject.WalletTransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WalletTransactionDto {
    private BigDecimal amount;
    private WalletTransactionType transactionType;
    private String referenceId;
    private ZonedDateTime createdAt;
}
