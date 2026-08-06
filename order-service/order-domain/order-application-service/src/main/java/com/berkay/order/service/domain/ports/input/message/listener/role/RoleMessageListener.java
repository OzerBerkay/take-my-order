package com.berkay.order.service.domain.ports.input.message.listener.role;

import com.berkay.order.service.domain.dto.message.RoleEventPayload;

public interface RoleMessageListener {
    void roleCreated(RoleEventPayload payload);
    void roleUpdated(RoleEventPayload payload);
    void roleDeleted(RoleEventPayload payload);
}
