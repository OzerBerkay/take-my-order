package com.berkay.order.service.domain.exception;

public class OrderNotDeliveredException extends OrderDomainException {

    public OrderNotDeliveredException(String message) {
        super("ORDER_NOT_DELIVERED", message);
    }

    public OrderNotDeliveredException(String message, Throwable cause) {
        super("ORDER_NOT_DELIVERED", message, cause);
    }
}

