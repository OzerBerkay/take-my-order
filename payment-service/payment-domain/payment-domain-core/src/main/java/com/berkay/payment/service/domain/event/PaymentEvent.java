package com.berkay.payment.service.domain.event;

import com.berkay.domain.event.DomainEvent;
import com.berkay.payment.service.domain.entity.Payment;
import com.berkay.payment.service.domain.entity.Wallet;
import com.berkay.payment.service.domain.entity.WalletTransaction;

import java.time.ZonedDateTime;
import java.util.List;

public abstract class PaymentEvent implements DomainEvent<Payment> {

    private final Payment payment;
    private final ZonedDateTime createdAt;
    private final List<String> failureMessages;
    private final WalletTransaction walletTransaction;
    private final Wallet wallet;

    public PaymentEvent(Payment payment, ZonedDateTime createdAt, List<String> failureMessages, WalletTransaction walletTransaction, Wallet wallet) {
        this.payment = payment;
        this.createdAt = createdAt;
        this.failureMessages = failureMessages;
        this.walletTransaction = walletTransaction;
        this.wallet = wallet;
    }

    public Payment getPayment() {
        return payment;
    }

    public ZonedDateTime getCreatedAt() {
        return createdAt;
    }

    public List<String> getFailureMessages() {
        return failureMessages;
    }

    public WalletTransaction getWalletTransaction() {
        return walletTransaction;
    }

    public Wallet getWallet() {
        return wallet;
    }
}