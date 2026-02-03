package com.berkay.identity.service.dataaccess.user.exception;

public class IdentityDataaccessException extends RuntimeException {
    public IdentityDataaccessException(String message) {
        super(message);
    }

    public IdentityDataaccessException(String message, Throwable cause) {
        super(message, cause);
    }
}