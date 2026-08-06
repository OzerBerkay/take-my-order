package com.berkay.identity.service.domain.event;

import com.berkay.identity.service.domain.entity.Role;
import java.time.ZonedDateTime;

public class RoleUpdatedEvent extends RoleEvent {
    public RoleUpdatedEvent(Role role, ZonedDateTime createdAt) {
        super(role, createdAt);
    }
}