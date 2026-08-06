package com.berkay.identity.service.dto.command.role;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.Set;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class UpdateRoleCommand {
    private final UUID roleId;

    @NotBlank(message = "Role name cannot be empty")
    private final String name;

    @NotEmpty(message = "At least one permission is required")
    private final Set<UUID> permissionIds;

    private final UUID organizationalUnitId;
}