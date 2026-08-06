package com.berkay.payment.service.domain.ports.input.message.listener.role;

import com.berkay.payment.service.domain.dto.message.RoleEventPayload;

public interface RoleMessageListener {
    void roleCreated(RoleEventPayload roleEventPayload);
    void roleUpdated(RoleEventPayload roleEventPayload);
    void roleDeleted(RoleEventPayload payload);
}
