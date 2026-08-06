package com.berkay.identity.service.dto.query;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class RoleResponse {
    private final UUID id;
    private final String name;
    private final boolean isStatic;
    private final UUID organizationalUnitId;
    private final String userType;
    private final List<PermissionResponse> permissions;
    private final ZonedDateTime createdAt;
}
