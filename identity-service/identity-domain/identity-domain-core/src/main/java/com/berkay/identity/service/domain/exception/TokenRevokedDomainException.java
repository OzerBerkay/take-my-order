package com.berkay.identity.service.domain.exception;

import lombok.Getter;

@Getter
public class TokenRevokedDomainException extends IdentityDomainException {
    
    private final String errorCode;

    public TokenRevokedDomainException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public TokenRevokedDomainException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
}
