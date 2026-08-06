package com.berkay.order.service.domain.ports.input.message.listener.permission;

import com.berkay.order.service.domain.dto.message.PermissionEventPayload;

public interface PermissionMessageListener {
    void permissionUpdated(PermissionEventPayload payload);
}
