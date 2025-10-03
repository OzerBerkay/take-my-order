package com.berkay.payment.service.domain.exception;

import com.berkay.domain.exception.DomainException;

public class PaymentApplicationServiceException extends DomainException {

    public PaymentApplicationServiceException(String message) {
        super(message);
    }

    public PaymentApplicationServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}