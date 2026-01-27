package com.berkay.payment.service.domain.event;

import com.berkay.payment.service.domain.entity.CreditHistory;
import com.berkay.payment.service.domain.entity.Payment;

import java.time.ZonedDateTime;
import java.util.Collections;

public class PaymentCompletedEvent extends PaymentEvent {

    private final CreditHistory creditHistory;

    public PaymentCompletedEvent(Payment payment,
                                 ZonedDateTime createdAt,
                                 CreditHistory creditHistory) {
        super(payment, createdAt, Collections.emptyList());
        this.creditHistory = creditHistory;
    }

    public CreditHistory getCreditHistory() {
        return creditHistory;
    }
}