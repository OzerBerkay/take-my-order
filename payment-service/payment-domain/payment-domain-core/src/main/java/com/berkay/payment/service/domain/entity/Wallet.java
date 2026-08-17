package com.berkay.payment.service.domain.entity;

import com.berkay.domain.entity.AggregateRoot;
import com.berkay.domain.valueobject.Money;
import com.berkay.payment.service.domain.valueobject.OwnerType;
import com.berkay.payment.service.domain.valueobject.WalletId;

import java.util.UUID;

public abstract class Wallet extends AggregateRoot<WalletId> {
    private final UUID ownerId;
    private final OwnerType ownerType;
    private Money balance;

    protected Wallet(WalletId walletId, UUID ownerId, OwnerType ownerType, Money balance) {
        super.setId(walletId);
        this.ownerId = ownerId;
        this.ownerType = ownerType;
        this.balance = balance;
    }

    public UUID getOwnerId() {
        return ownerId;
    }

    public OwnerType getOwnerType() {
        return ownerType;
    }

    public Money getBalance() {
        return balance;
    }

    protected void setBalance(Money balance) {
        this.balance = balance;
    }

    public void addBalance(Money amount) {
        if (amount == null || !amount.isGreaterThanZero()) {
            throw new IllegalArgumentException("Amount to add must be greater than zero");
        }
        this.balance = this.balance.add(amount);
    }

    public abstract void subtractBalance(Money amount);
}
