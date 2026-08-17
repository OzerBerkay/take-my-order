package com.berkay.payment.service.dataaccess.wallet.adapter;

import com.berkay.payment.service.dataaccess.wallet.mapper.WalletDataAccessMapper;
import com.berkay.payment.service.dataaccess.wallet.repository.WalletJpaRepository;
import com.berkay.payment.service.dataaccess.wallet.entity.WalletEntity;
import com.berkay.payment.service.domain.entity.Wallet;
import com.berkay.payment.service.domain.ports.output.repository.WalletRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class WalletRepositoryImpl implements WalletRepository {

    private final WalletJpaRepository walletJpaRepository;
    private final WalletDataAccessMapper walletDataAccessMapper;

    public WalletRepositoryImpl(WalletJpaRepository walletJpaRepository,
                                WalletDataAccessMapper walletDataAccessMapper) {
        this.walletJpaRepository = walletJpaRepository;
        this.walletDataAccessMapper = walletDataAccessMapper;
    }

    @Override
    public Wallet save(Wallet wallet) {
        Optional<WalletEntity> existingWalletEntity = walletJpaRepository.findById(wallet.getId().getValue());
        WalletEntity walletEntity;
        if (existingWalletEntity.isPresent()) {
            walletEntity = existingWalletEntity.get();
            walletEntity.setBalance(wallet.getBalance().getAmount());
            walletEntity.setOwnerId(wallet.getOwnerId());
            walletEntity.setOwnerType(wallet.getOwnerType());
        } else {
            walletEntity = walletDataAccessMapper.walletToWalletEntity(wallet);
        }
        return walletDataAccessMapper.walletEntityToWallet(walletJpaRepository.save(walletEntity));
    }

    @Override
    public Optional<Wallet> findByOwnerId(UUID ownerId) {
        return walletJpaRepository.findByOwnerId(ownerId)
                .map(walletDataAccessMapper::walletEntityToWallet);
    }

    @Override
    public Optional<Wallet> findByOwnerIdWithLock(UUID ownerId) {
        return walletJpaRepository.findByOwnerIdWithLock(ownerId)
                .map(walletDataAccessMapper::walletEntityToWallet);
    }
}
