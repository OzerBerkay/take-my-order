package com.berkay.restaurant.service.domain.ports.output.message.publisher;

import com.berkay.domain.event.publisher.DomainEventPublisher;
import com.berkay.restaurant.service.domain.event.OrderApprovedEvent;

public interface OrderApprovedMessagePublisher extends DomainEventPublisher<OrderApprovedEvent> {
}
