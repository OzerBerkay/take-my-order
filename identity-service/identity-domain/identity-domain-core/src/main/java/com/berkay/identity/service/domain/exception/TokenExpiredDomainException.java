package com.berkay.identity.service.domain.exception;

public class TokenExpiredDomainException extends RuntimeException {
    
    private final String errorCode;

    public TokenExpiredDomainException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
