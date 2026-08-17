package com.berkay.payment.service.domain.strategy;

import com.berkay.payment.service.domain.entity.Payment;
import com.berkay.payment.service.domain.event.PaymentEvent;
import com.berkay.payment.service.domain.exception.PaymentDomainException;

import java.util.List;

public interface PaymentProcessorStrategy {
    
    /**
     * Processes a payment by deducting funds or charging a card, depending on the strategy.
     * @param payment The payment details.
     * @param failureMessages A list to collect validation/processing errors.
     * @return A PaymentEvent (Completed or Failed).
     */
    PaymentEvent processPayment(Payment payment, List<String> failureMessages);

    /**
     * Refunds or cancels a previously processed payment.
     * @param payment The payment details.
     * @param failureMessages A list to collect validation/processing errors.
     * @return A PaymentEvent (Cancelled or Failed).
     */
    PaymentEvent refundPayment(Payment payment, List<String> failureMessages);
    
    /**
     * Indicates whether this strategy can handle the given payment method.
     * For now, this is fixed to WALLET, but can be expanded.
     */
    boolean supports(String paymentMethod);
}
