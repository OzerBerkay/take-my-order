package com.berkay.order.service.domain.exception;

import com.berkay.domain.exception.DomainException;

public class OrderDomainException extends DomainException {

    private final String errorCode;

    public OrderDomainException(String message) {
        super(message);
        this.errorCode = null;
    }

    public OrderDomainException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public OrderDomainException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = null;
    }

    public OrderDomainException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}

