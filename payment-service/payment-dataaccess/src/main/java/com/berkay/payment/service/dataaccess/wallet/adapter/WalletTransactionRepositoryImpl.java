package com.berkay.payment.service.dataaccess.wallet.adapter;

import com.berkay.payment.service.dataaccess.wallet.entity.WalletTransactionEntity;
import com.berkay.payment.service.dataaccess.wallet.mapper.WalletDataAccessMapper;
import com.berkay.payment.service.dataaccess.wallet.repository.WalletJpaRepository;
import com.berkay.payment.service.dataaccess.wallet.repository.WalletTransactionJpaRepository;
import com.berkay.payment.service.domain.entity.WalletTransaction;
import com.berkay.payment.service.domain.ports.output.repository.WalletTransactionRepository;
import com.berkay.payment.service.domain.valueobject.DomainPagedResult;
import com.berkay.payment.service.domain.valueobject.WalletTransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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

    @Override
    public DomainPagedResult<WalletTransaction> findByWalletIdPaged(
            UUID walletId,
            WalletTransactionType type,
            int page,
            int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<WalletTransactionEntity> pagedEntities;
        if (type == null) {
            pagedEntities = walletTransactionJpaRepository.findByWalletIdOrderByCreatedAtDesc(walletId, pageable);
        } else {
            pagedEntities = walletTransactionJpaRepository.findByWalletIdAndTransactionTypeOrderByCreatedAtDesc(walletId, type, pageable);
        }

        List<WalletTransaction> domainList = pagedEntities.getContent().stream()
                .map(walletDataAccessMapper::walletTransactionEntityToWalletTransaction)
                .collect(Collectors.toList());

        return new DomainPagedResult<>(
                domainList,
                pagedEntities.getNumber(),
                pagedEntities.getSize(),
                pagedEntities.getTotalElements(),
                pagedEntities.getTotalPages()
        );
    }
}
