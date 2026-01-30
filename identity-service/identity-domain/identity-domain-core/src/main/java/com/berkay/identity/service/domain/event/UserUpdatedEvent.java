package com.berkay.identity.service.domain.event;

import com.berkay.identity.service.domain.entity.User;
import java.time.ZonedDateTime;

public class UserUpdatedEvent extends UserEvent {

    public UserUpdatedEvent(User user, ZonedDateTime createdAt) {
        super(user, createdAt);
    }
}