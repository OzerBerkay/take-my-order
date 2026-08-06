package com.berkay.application.exception;

public class TokenRevokedException extends RuntimeException {
    private final String errorCode;

    public TokenRevokedException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
