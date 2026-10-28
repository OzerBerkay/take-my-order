package com.berkay.order.service.domain.exception;

public class ReviewAlreadyExistsException extends OrderDomainException {

    public ReviewAlreadyExistsException(String message) {
        super("REVIEW_ALREADY_EXISTS", message);
    }

    public ReviewAlreadyExistsException(String message, Throwable cause) {
        super("REVIEW_ALREADY_EXISTS", message, cause);
    }
}

