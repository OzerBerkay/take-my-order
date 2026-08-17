package com.berkay.payment.service.dataaccess.wallet.adapter;

import com.berkay.payment.service.dataaccess.wallet.mapper.WalletDataAccessMapper;
import com.berkay.payment.service.dataaccess.wallet.repository.WalletTransactionJpaRepository;
import com.berkay.payment.service.dataaccess.wallet.repository.WalletJpaRepository;
import com.berkay.payment.service.dataaccess.wallet.entity.WalletTransactionEntity;
import com.berkay.payment.service.domain.entity.WalletTransaction;
import com.berkay.payment.service.domain.ports.output.repository.WalletTransactionRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class WalletTransactionRepositoryImpl implements WalletTransactionRepository {

    private final WalletTransactionJpaRepository walletTransactionJpaRepository;
    private final WalletDataAccessMapper walletDataAccessMapper;
    private final WalletJpaRepository walletJpaRepository;

    public WalletTransactionRepositoryImpl(WalletTransactionJpaRepository walletTransactionJpaRepository,
                                           WalletDataAccessMapper walletDataAccessMapper,
                                           WalletJpaRepository walletJpaRepository) {
        this.walletTransactionJpaRepository = walletTransactionJpaRepository;
        this.walletDataAccessMapper = walletDataAccessMapper;
        this.walletJpaRepository = walletJpaRepository;
    }

    @Override
    public WalletTransaction save(WalletTransaction walletTransaction) {
        WalletTransactionEntity entity = walletDataAccessMapper.walletTransactionToWalletTransactionEntity(walletTransaction);
        entity.setWallet(walletJpaRepository.getReferenceById(walletTransaction.getWalletId().getValue()));
        return walletDataAccessMapper.walletTransactionEntityToWalletTransaction(
                walletTransactionJpaRepository.save(entity)
        );
    }

    @Override
    public List<WalletTransaction> findByWalletId(UUID walletId) {
        return walletTransactionJpaRepository.findByWalletId(walletId).stream()
                .map(walletDataAccessMapper::walletTransactionEntityToWalletTransaction)
                .collect(Collectors.toList());
    }
}
