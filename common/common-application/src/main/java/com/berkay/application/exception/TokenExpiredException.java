package com.berkay.application.exception;

public class TokenExpiredException extends RuntimeException {
    
    private final String errorCode;

    public TokenExpiredException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
