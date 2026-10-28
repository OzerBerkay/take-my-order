package com.berkay.order.service.domain.exception;

public class ReviewNotFoundException extends OrderDomainException {

    public ReviewNotFoundException(String message) {
        super("REVIEW_NOT_FOUND", message);
    }

    public ReviewNotFoundException(String message, Throwable cause) {
        super("REVIEW_NOT_FOUND", message, cause);
    }
}

