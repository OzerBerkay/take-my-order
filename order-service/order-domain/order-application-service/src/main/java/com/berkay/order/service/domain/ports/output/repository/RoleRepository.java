package com.berkay.order.service.domain.ports.output.repository;

import com.berkay.order.service.domain.dto.message.PermissionEventPayload;
import com.berkay.order.service.domain.dto.message.RoleEventPayload;

import java.util.UUID;

public interface RoleRepository {
    void save(RoleEventPayload payload);
    void updatePermission(PermissionEventPayload payload);
    void delete(UUID roleId);
}
