package com.berkay.payment.service.domain.ports.input.message.listener.permission;

import com.berkay.payment.service.domain.dto.message.PermissionEventPayload;

public interface PermissionMessageListener {
    void permissionUpdated(PermissionEventPayload payload);
}
