package com.berkay.payment.service.domain.ports.output.repository;

import com.berkay.payment.service.domain.entity.WalletTransaction;

import java.util.List;
import java.util.UUID;

public interface WalletTransactionRepository {
    WalletTransaction save(WalletTransaction walletTransaction);
    List<WalletTransaction> findByWalletId(UUID walletId);
}
