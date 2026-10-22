package com.berkay.order.service.domain.exception;

import com.berkay.domain.exception.DomainException;

public class OrderConflictException extends DomainException {

    public OrderConflictException(String message) {
        super(message);
    }

    public OrderConflictException(String message, Throwable cause) {
        super(message, cause);
    }
}
