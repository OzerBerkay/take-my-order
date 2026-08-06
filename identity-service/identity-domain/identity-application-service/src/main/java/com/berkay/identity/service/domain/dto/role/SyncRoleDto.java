package com.berkay.identity.service.domain.dto.role;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class SyncRoleDto {
    private final UUID id;
    private final String name;
    private final String userType;
    private final UUID organizationalUnitId;
    private final List<SyncPermissionDto> permissions;
    private final ZonedDateTime updatedAt;
}
