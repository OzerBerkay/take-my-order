package com.berkay.payment.service.dataaccess.wallet.repository;

import com.berkay.payment.service.dataaccess.wallet.entity.WalletTransactionEntity;
import com.berkay.payment.service.domain.valueobject.WalletTransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface WalletTransactionJpaRepository extends JpaRepository<WalletTransactionEntity, UUID> {
    List<WalletTransactionEntity> findByWalletId(UUID walletId);
    Page<WalletTransactionEntity> findByWalletIdOrderByCreatedAtDesc(UUID walletId, Pageable pageable);
    Page<WalletTransactionEntity> findByWalletIdAndTransactionTypeOrderByCreatedAtDesc(UUID walletId, WalletTransactionType transactionType, Pageable pageable);
}
