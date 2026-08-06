package com.berkay.restaurant.service.domain.ports.input.message.listener.role;

import com.berkay.restaurant.service.domain.dto.message.RoleEventPayload;

public interface RoleMessageListener {
    void roleCreated(RoleEventPayload payload);
    void roleUpdated(RoleEventPayload payload);
    void roleDeleted(RoleEventPayload payload);
}
