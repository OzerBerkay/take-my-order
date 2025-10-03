package com.berkay.payment.service.domain.ports.output.message.publisher;

import com.berkay.domain.event.publisher.DomainEventPublisher;
import com.berkay.payment.service.domain.event.PaymentCompletedEvent;

public interface PaymentCompletedMessagePublisher extends DomainEventPublisher<PaymentCompletedEvent> {
}
