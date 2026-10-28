package com.berkay.order.service.domain.exception;

public class ReviewTimeExpiredException extends OrderDomainException {

    public ReviewTimeExpiredException(String message) {
        super("REVIEW_TIME_EXPIRED", message);
    }

    public ReviewTimeExpiredException(String message, Throwable cause) {
        super("REVIEW_TIME_EXPIRED", message, cause);
    }
}

