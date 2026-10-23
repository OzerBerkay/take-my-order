package com.berkay.payment.service.domain.dto.wallet;

import com.fasterxml.jackson.annotation.JsonProperty;
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
    private List<WalletTransactionDto> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private boolean last;

    @JsonProperty("transactions")
    public List<WalletTransactionDto> getTransactions() {
        return content;
    }
}
