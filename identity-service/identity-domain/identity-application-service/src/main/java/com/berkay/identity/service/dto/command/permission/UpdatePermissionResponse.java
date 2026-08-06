package com.berkay.identity.service.dto.command.permission;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class UpdatePermissionResponse {
    private final UUID permissionId;
}
