package com.berkay.identity.service.dto.command;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record UnassignUserRoleCommand(
        @NotNull(message = "User ID is required")
        UUID userId,
        
        @NotNull(message = "Role ID is required")
        UUID roleId,
        
        java.util.UUID requesterId,
        com.berkay.identity.service.domain.valueobject.UserType requesterUserType,
        java.util.List<java.util.UUID> requesterRoleIds
) {}
