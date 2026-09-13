package com.berkay.application.exception;

public class InvalidTokenException extends RuntimeException {

    private final String errorCode;

    public InvalidTokenException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
