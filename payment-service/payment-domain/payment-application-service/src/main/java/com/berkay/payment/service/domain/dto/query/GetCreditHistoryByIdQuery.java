package com.berkay.payment.service.domain.dto.query;

import com.berkay.payment.service.domain.exception.PaymentDomainException;

import java.util.UUID;

public record GetCreditHistoryByIdQuery(UUID creditHistoryId) {
    public GetCreditHistoryByIdQuery {
        if (creditHistoryId == null) {
            throw new PaymentDomainException("Credit History ID cannot be null!");
        }
    }
}
