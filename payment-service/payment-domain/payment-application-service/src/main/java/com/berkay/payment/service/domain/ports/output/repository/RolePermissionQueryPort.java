package com.berkay.payment.service.domain.ports.output.repository;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface RolePermissionQueryPort {
    Set<String> getPermissionCodesByRoleId(UUID roleId);
    Optional<UUID> getOrganizationalUnitIdByRoleId(UUID roleId);
}
