package com.berkay.restaurant.service.domain.ports.output.repository;

import com.berkay.restaurant.service.domain.dto.message.PermissionEventPayload;
import com.berkay.restaurant.service.domain.dto.message.RoleEventPayload;

import java.util.UUID;

public interface RoleRepository {
    void save(RoleEventPayload payload);
    void updatePermission(PermissionEventPayload payload);
    void delete(UUID roleId);
}
