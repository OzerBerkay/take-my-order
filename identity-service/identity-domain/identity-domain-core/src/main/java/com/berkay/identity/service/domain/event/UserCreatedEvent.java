package com.berkay.identity.service.domain.event;

import com.berkay.identity.service.domain.entity.User;
import java.time.ZonedDateTime;

public class UserCreatedEvent extends UserEvent {

    public UserCreatedEvent(User user, ZonedDateTime createdAt) {
        super(user, createdAt);
    }
}