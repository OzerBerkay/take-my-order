package com.berkay.restaurant.service.domain.ports.output.message.publisher;

import com.berkay.domain.event.publisher.DomainEventPublisher;
import com.berkay.restaurant.service.domain.event.OrderRejectedEvent;

public interface OrderRejectedMessagePublisher extends DomainEventPublisher<OrderRejectedEvent> {
}
