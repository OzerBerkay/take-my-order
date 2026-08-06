package com.berkay.restaurant.service.domain.ports.input.message.listener.permission;

import com.berkay.restaurant.service.domain.dto.message.PermissionEventPayload;

public interface PermissionMessageListener {
    void permissionUpdated(PermissionEventPayload payload);
}
