package com.berkay.payment.service.dataaccess.wallet.mapper;

import com.berkay.domain.valueobject.Money;
import com.berkay.payment.service.dataaccess.wallet.entity.WalletEntity;
import com.berkay.payment.service.dataaccess.wallet.entity.WalletTransactionEntity;
import com.berkay.payment.service.domain.entity.CustomerWallet;
import com.berkay.payment.service.domain.entity.RestaurantWallet;
import com.berkay.payment.service.domain.entity.Wallet;
import com.berkay.payment.service.domain.entity.WalletTransaction;
import com.berkay.payment.service.domain.valueobject.OwnerType;
import com.berkay.payment.service.domain.valueobject.WalletId;
import com.berkay.payment.service.domain.valueobject.WalletTransactionId;
import org.springframework.stereotype.Component;

@Component
public class WalletDataAccessMapper {

    public WalletEntity walletToWalletEntity(Wallet wallet) {
        return WalletEntity.builder()
                .id(wallet.getId().getValue())
                .ownerId(wallet.getOwnerId())
                .ownerType(wallet.getOwnerType())
                .balance(wallet.getBalance().getAmount())
                .build();
    }

    public Wallet walletEntityToWallet(WalletEntity walletEntity) {
        if (walletEntity.getOwnerType() == OwnerType.CUSTOMER) {
            return new CustomerWallet(
                    new WalletId(walletEntity.getId()),
                    walletEntity.getOwnerId(),
                    new Money(walletEntity.getBalance())
            );
        } else if (walletEntity.getOwnerType() == OwnerType.RESTAURANT) {
            return new RestaurantWallet(
                    new WalletId(walletEntity.getId()),
                    walletEntity.getOwnerId(),
                    new Money(walletEntity.getBalance())
            );
        }
        throw new IllegalArgumentException("Unknown OwnerType: " + walletEntity.getOwnerType());
    }

    public WalletTransactionEntity walletTransactionToWalletTransactionEntity(WalletTransaction walletTransaction) {
        return WalletTransactionEntity.builder()
                .id(walletTransaction.getId().getValue())
                .wallet(WalletEntity.builder().id(walletTransaction.getWalletId().getValue()).build())
                .amount(walletTransaction.getAmount().getAmount())
                .transactionType(walletTransaction.getTransactionType())
                .referenceId(walletTransaction.getReferenceId())
                .createdAt(walletTransaction.getCreatedAt())
                .build();
    }

    public WalletTransaction walletTransactionEntityToWalletTransaction(WalletTransactionEntity walletTransactionEntity) {
        return WalletTransaction.builder()
                .walletTransactionId(new WalletTransactionId(walletTransactionEntity.getId()))
                .walletId(new WalletId(walletTransactionEntity.getWallet().getId()))
                .amount(new Money(walletTransactionEntity.getAmount()))
                .transactionType(walletTransactionEntity.getTransactionType())
                .referenceId(walletTransactionEntity.getReferenceId())
                .createdAt(walletTransactionEntity.getCreatedAt())
                .build();
    }
}
