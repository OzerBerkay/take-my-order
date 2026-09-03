package com.berkay.identity.service.domain.exception;

public class InvalidCredentialsException extends IdentityDomainException {
    private final String errorCode;

    public InvalidCredentialsException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
