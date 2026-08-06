package com.berkay.identity.service.outbox.model.permission;

import com.berkay.identity.service.outbox.model.role.PermissionPayload;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class PermissionEventPayload {
    private final String eventType;
    private final PermissionPayload permission;
}
