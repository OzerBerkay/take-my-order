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
    private final WalletTransaction customerWalletTransaction;
    private final Wallet customerWallet;
    private final WalletTransaction restaurantWalletTransaction;
    private final Wallet restaurantWallet;

    public PaymentEvent(Payment payment, ZonedDateTime createdAt, List<String> failureMessages, WalletTransaction customerWalletTransaction, Wallet customerWallet, WalletTransaction restaurantWalletTransaction, Wallet restaurantWallet) {
        this.payment = payment;
        this.createdAt = createdAt;
        this.failureMessages = failureMessages;
        this.customerWalletTransaction = customerWalletTransaction;
        this.customerWallet = customerWallet;
        this.restaurantWalletTransaction = restaurantWalletTransaction;
        this.restaurantWallet = restaurantWallet;
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

    public WalletTransaction getCustomerWalletTransaction() {
        return customerWalletTransaction;
    }

    public Wallet getCustomerWallet() {
        return customerWallet;
    }

    public WalletTransaction getRestaurantWalletTransaction() {
        return restaurantWalletTransaction;
    }

    public Wallet getRestaurantWallet() {
        return restaurantWallet;
    }
}