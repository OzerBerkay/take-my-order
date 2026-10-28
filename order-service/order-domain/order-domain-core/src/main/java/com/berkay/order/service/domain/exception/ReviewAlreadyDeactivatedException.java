package com.berkay.order.service.domain.exception;

public class ReviewAlreadyDeactivatedException extends OrderDomainException {

    public ReviewAlreadyDeactivatedException(String message) {
        super("REVIEW_ALREADY_DEACTIVATED", message);
    }

    public ReviewAlreadyDeactivatedException(String message, Throwable cause) {
        super("REVIEW_ALREADY_DEACTIVATED", message, cause);
    }
}

