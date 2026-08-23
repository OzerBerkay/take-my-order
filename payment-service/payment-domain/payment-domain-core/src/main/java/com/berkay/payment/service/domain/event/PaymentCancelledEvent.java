package com.berkay.payment.service.domain.event;

import com.berkay.payment.service.domain.entity.Payment;
import com.berkay.payment.service.domain.entity.Wallet;
import com.berkay.payment.service.domain.entity.WalletTransaction;

import java.time.ZonedDateTime;
import java.util.Collections;

public class PaymentCancelledEvent extends PaymentEvent {

    public PaymentCancelledEvent(Payment payment, ZonedDateTime createdAt, WalletTransaction customerWalletTransaction, Wallet customerWallet, WalletTransaction restaurantWalletTransaction, Wallet restaurantWallet) {
        super(payment, createdAt, Collections.emptyList(), customerWalletTransaction, customerWallet, restaurantWalletTransaction, restaurantWallet);
    }
}
