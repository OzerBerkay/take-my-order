package com.berkay.payment.service.domain.event;

import com.berkay.payment.service.domain.entity.Payment;
import com.berkay.payment.service.domain.entity.Wallet;
import com.berkay.payment.service.domain.entity.WalletTransaction;

import java.time.ZonedDateTime;
import java.util.Collections;

public class PaymentCompletedEvent extends PaymentEvent {
    
    public PaymentCompletedEvent(Payment payment, ZonedDateTime createdAt, WalletTransaction walletTransaction, Wallet wallet) {
        super(payment, createdAt, Collections.emptyList(), walletTransaction, wallet);
    }
}