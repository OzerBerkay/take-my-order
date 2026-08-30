package com.berkay.identity.service.domain.exception;

public class UserAlreadyExistsException extends IdentityDomainException {
    private final String errorCode;

    public UserAlreadyExistsException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
