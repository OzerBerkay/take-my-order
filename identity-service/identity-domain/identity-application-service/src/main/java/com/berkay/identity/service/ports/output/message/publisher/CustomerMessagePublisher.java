package com.berkay.identity.service.ports.output.message.publisher;

import com.berkay.identity.service.domain.entity.User;

public interface CustomerMessagePublisher {
    void publish(User user);
}
