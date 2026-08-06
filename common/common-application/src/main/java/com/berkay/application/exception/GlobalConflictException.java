package com.berkay.application.exception;

public class GlobalConflictException extends RuntimeException {
    public GlobalConflictException(String message) {
        super(message);
    }

    public GlobalConflictException(String message, Throwable cause) {
        super(message, cause);
    }
}
