package com.berkay.identity.service.domain.exception;

import com.berkay.domain.exception.DomainException;

public class IdentityDomainException extends DomainException {

    public IdentityDomainException(String message) {
        super(message);
    }

    public IdentityDomainException(String message, Throwable cause) {
        super(message, cause);
    }
}