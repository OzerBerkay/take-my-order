package com.berkay.payment.service.domain.dto.query;

import com.berkay.payment.service.domain.exception.PaymentDomainException;

import java.util.UUID;

public record GetCreditHistoryQuery(UUID customerId, int page, int size) {
    public GetCreditHistoryQuery {
        if (customerId == null) {
            throw new PaymentDomainException("Customer ID cannot be null");
        }
        if (page < 0) {
            throw new PaymentDomainException("Page number cannot be less than 0");
        }
        if (size < 1) {
            throw new PaymentDomainException("Page size cannot be less than 1");
        }
    }
}
