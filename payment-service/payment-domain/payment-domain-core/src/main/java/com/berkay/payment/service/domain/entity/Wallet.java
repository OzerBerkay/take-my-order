package com.berkay.payment.service.domain.entity;

import com.berkay.domain.entity.AggregateRoot;
import com.berkay.domain.valueobject.Money;
import com.berkay.payment.service.domain.exception.PaymentDomainException;
import com.berkay.payment.service.domain.valueobject.OwnerType;
import com.berkay.payment.service.domain.valueobject.WalletId;

import java.math.BigDecimal;
import java.util.UUID;

public abstract class Wallet extends AggregateRoot<WalletId> {
    public static final Money MAX_DEPOSIT_AMOUNT = new Money(new BigDecimal("99999.99"));

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
            throw new PaymentDomainException("Amount to add must be greater than zero!");
        }
        if (amount.isGreaterThan(MAX_DEPOSIT_AMOUNT)) {
            throw new PaymentDomainException("Deposit amount cannot exceed " + MAX_DEPOSIT_AMOUNT.getAmount() + "!");
        }
        this.balance = this.balance.add(amount);
    }

    public abstract void subtractBalance(Money amount);
}
