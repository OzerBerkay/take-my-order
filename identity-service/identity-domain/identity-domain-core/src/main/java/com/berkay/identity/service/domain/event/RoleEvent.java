package com.berkay.identity.service.domain.event;

import com.berkay.domain.event.DomainEvent;
import com.berkay.identity.service.domain.entity.Role;

import java.time.ZonedDateTime;

public abstract class RoleEvent implements DomainEvent<Role> {
    private final Role role;
    private final ZonedDateTime createdAt;

    public RoleEvent(Role role, ZonedDateTime createdAt) {
        this.role = role;
        this.createdAt = createdAt;
    }

    public Role getRole() {
        return role;
    }

    public ZonedDateTime getCreatedAt() {
        return createdAt;
    }
}
