package com.berkay.payment.service.domain.ports.output.repository;

import com.berkay.payment.service.domain.entity.WalletTransaction;

import com.berkay.payment.service.domain.valueobject.DomainPagedResult;
import com.berkay.payment.service.domain.valueobject.WalletTransactionType;

import java.util.List;
import java.util.UUID;

public interface WalletTransactionRepository {
    WalletTransaction save(WalletTransaction walletTransaction);
    List<WalletTransaction> findByWalletId(UUID walletId);
    DomainPagedResult<WalletTransaction> findByWalletIdPaged(UUID walletId, WalletTransactionType type, int page, int size);
}
