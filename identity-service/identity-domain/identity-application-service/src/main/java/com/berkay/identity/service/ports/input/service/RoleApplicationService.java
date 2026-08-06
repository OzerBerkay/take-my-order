package com.berkay.identity.service.ports.input.service;

import com.berkay.identity.service.dto.command.role.*;
import com.berkay.identity.service.domain.dto.role.SyncRolesQuery;
import com.berkay.identity.service.domain.dto.role.SyncRolesResponse;

import java.util.UUID;

public interface RoleApplicationService {
    CreateRoleResponse createRole(CreateRoleCommand command);
    UpdateRoleResponse updateRole(UpdateRoleCommand command);
    DeleteRoleResponse deleteRole(DeleteRoleCommand command);
    SyncRolesResponse syncRoles(SyncRolesQuery query);

    // GET Endpoints
    com.berkay.identity.service.dto.query.PageResult<com.berkay.identity.service.dto.query.RoleResponse> getAdminRoles(int page, int size, String name, java.util.UUID orgUnitId, String userType);

    com.berkay.identity.service.dto.query.PageResult<com.berkay.identity.service.dto.query.RoleResponse> getMerchantRoles(int page, int size, String name, java.util.UUID orgUnitId, java.util.List<java.util.UUID> authorizedOrgUnitIds);

    com.berkay.identity.service.dto.query.RoleResponse getRoleById(UUID roleId, java.util.List<UUID> authorizedOrgUnitIds);


}