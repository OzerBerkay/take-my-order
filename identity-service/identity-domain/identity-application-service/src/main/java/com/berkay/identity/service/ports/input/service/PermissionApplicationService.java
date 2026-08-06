package com.berkay.identity.service.ports.input.service;


import com.berkay.identity.service.dto.command.permission.UpdatePermissionCommand;
import com.berkay.identity.service.dto.command.permission.UpdatePermissionResponse;

public interface PermissionApplicationService {
    UpdatePermissionResponse updatePermission(UpdatePermissionCommand command);

    // GET Endpoints
    com.berkay.identity.service.dto.query.PageResult<com.berkay.identity.service.dto.query.PermissionResponse> getAdminPermissions(int page, int size);

    com.berkay.identity.service.dto.query.PageResult<com.berkay.identity.service.dto.query.PermissionResponse> getMerchantPermissions(int page, int size);

    java.util.Map<String, java.util.List<com.berkay.identity.service.dto.query.PermissionResponse>> getGroupedPermissions(boolean isAdmin);
}
