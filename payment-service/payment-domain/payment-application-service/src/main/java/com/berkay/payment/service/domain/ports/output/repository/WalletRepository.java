package com.berkay.payment.service.domain.ports.output.repository;

import com.berkay.payment.service.domain.entity.Wallet;

import java.util.Optional;
import java.util.UUID;

public interface WalletRepository {
    Wallet save(Wallet wallet);
    Optional<Wallet> findByOwnerId(UUID ownerId);
    Optional<Wallet> findByOwnerIdWithLock(UUID ownerId);
}
