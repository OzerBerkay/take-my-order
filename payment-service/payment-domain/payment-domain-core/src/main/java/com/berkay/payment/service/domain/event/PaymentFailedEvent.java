package com.berkay.payment.service.domain.event;

import com.berkay.payment.service.domain.entity.Payment;
import com.berkay.payment.service.domain.entity.Wallet;

import java.time.ZonedDateTime;
import java.util.List;

public class PaymentFailedEvent extends PaymentEvent {

    public PaymentFailedEvent(Payment payment,
                              ZonedDateTime createdAt,
                              List<String> failureMessages,
                              Wallet wallet) {
        super(payment, createdAt, failureMessages, null, wallet);
    }
}
