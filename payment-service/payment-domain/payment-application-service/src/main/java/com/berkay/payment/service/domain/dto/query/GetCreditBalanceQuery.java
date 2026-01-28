package com.berkay.payment.service.domain.dto.query;

import com.berkay.payment.service.domain.exception.PaymentDomainException;

import java.util.UUID;

public record GetCreditBalanceQuery(UUID customerId) {
    public GetCreditBalanceQuery {
        if (customerId == null) {
            throw new PaymentDomainException("CustomerId cannot be null!");
        }
    }
}
