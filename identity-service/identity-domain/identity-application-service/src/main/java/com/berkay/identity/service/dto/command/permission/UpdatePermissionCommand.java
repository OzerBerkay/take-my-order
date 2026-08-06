package com.berkay.identity.service.dto.command.permission;

import com.berkay.identity.service.domain.valueobject.DomainType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class UpdatePermissionCommand {
    @NotNull(message = "Permission ID is required")
    private final UUID permissionId;

    @NotNull(message = "Description is required")
    private final String description;

    @NotNull(message = "Active flag is required")
    private final Boolean active;
}
