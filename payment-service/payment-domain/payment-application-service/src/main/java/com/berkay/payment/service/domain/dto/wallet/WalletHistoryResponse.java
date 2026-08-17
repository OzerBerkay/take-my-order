package com.berkay.payment.service.domain.dto.wallet;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WalletHistoryResponse {
    private UUID ownerId;
    private List<WalletTransactionDto> transactions;
}
