package com.berkay.order.service.domain.ports.output.repository;

import java.util.Set;
import java.util.UUID;

public interface RolePermissionQueryPort {
    Set<String> getPermissionCodesByRoleId(UUID roleId);
}
