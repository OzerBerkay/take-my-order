package com.berkay.identity.service.domain.exception;

import com.berkay.domain.exception.DomainException;

public class IdentityDomainException extends DomainException {

    private final String errorCode;

    public IdentityDomainException(String message) {
        super(message);
        this.errorCode = null;
    }

    public IdentityDomainException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public IdentityDomainException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = null;
    }

    public IdentityDomainException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}