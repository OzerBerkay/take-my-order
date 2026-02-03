package com.berkay.identity.service.dataaccess.outbox.exception;

public class UserOutboxNotFoundException extends RuntimeException {
    public UserOutboxNotFoundException(String message) {
        super(message);
    }
}