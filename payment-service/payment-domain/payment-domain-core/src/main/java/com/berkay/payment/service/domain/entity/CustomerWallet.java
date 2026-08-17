package com.berkay.payment.service.domain.entity;

import com.berkay.domain.valueobject.Money;
import com.berkay.payment.service.domain.exception.PaymentDomainException;
import com.berkay.payment.service.domain.valueobject.OwnerType;
import com.berkay.payment.service.domain.valueobject.WalletId;

import java.util.UUID;

public class CustomerWallet extends Wallet {

    public CustomerWallet(WalletId walletId, UUID ownerId, Money balance) {
        super(walletId, ownerId, OwnerType.CUSTOMER, balance);
    }

    @Override
    public void subtractBalance(Money amount) {
        if (amount == null || !amount.isGreaterThanZero()) {
            throw new PaymentDomainException("Amount to subtract must be greater than zero!");
        }
        if (getBalance().isGreaterThan(amount) || getBalance().equals(amount)) {
            setBalance(getBalance().subtract(amount));
        } else {
            throw new PaymentDomainException("Customer does not have enough balance for this transaction!");
        }
    }
}
