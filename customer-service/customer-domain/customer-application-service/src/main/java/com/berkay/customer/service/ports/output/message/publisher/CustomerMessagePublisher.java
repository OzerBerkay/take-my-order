package com.berkay.customer.service.ports.output.message.publisher;

import com.berkay.customer.service.domain.event.CustomerCreatedEvent;

public interface CustomerMessagePublisher {

    void publish(CustomerCreatedEvent customerCreatedEvent);

}