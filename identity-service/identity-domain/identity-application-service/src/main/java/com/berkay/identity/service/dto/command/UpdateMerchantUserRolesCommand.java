package com.berkay.identity.service.dto.command;

import com.berkay.identity.service.domain.valueobject.UserType;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record UpdateMerchantUserRolesCommand(
        @NotNull(message = "User ID is required")
        UUID userId,

        @NotNull(message = "Organizational Unit ID is required")
        UUID orgUnitId,

        @NotNull(message = "Role IDs list cannot be null")
        List<UUID> roleIds,

        UUID requesterId,
        UserType requesterUserType,
        List<UUID> requesterRoleIds
) {}
