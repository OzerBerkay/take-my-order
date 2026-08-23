package com.berkay.payment.service.domain.dto.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class RoleEventPayload {
    private UUID roleId;
    private String name;
    private String userType;
    private UUID organizationalUnitId;
    private String eventType;
    private Long version;
    private java.util.List<PermissionPayload> permissions;
}
