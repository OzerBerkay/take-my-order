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
public class CreateRoleCommand {
    @NotBlank(message = "Role name cannot be empty")
    private final String name;

    @NotEmpty(message = "At least one permission is required")
    private final Set<UUID> permissionIds;

    // Customer ve Internal için null, Merchant için dolu (Restaurant ID)
    // Body'de validation'a girmez, Controller tarafından set edilir.
    private final UUID organizationalUnitId;
}
