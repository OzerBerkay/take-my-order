package com.berkay.identity.service.ports.output.repository;

import com.berkay.identity.service.dto.query.PageResult;
import com.berkay.identity.service.dto.query.RoleResponse;
import com.berkay.identity.service.dto.query.UserResponse;

import java.util.List;
import java.util.UUID;

public interface RoleQueryRepository {
    PageResult<RoleResponse> getAdminRoles(int page, int size, String name, java.util.UUID orgUnitId, String userType);
    PageResult<RoleResponse> getMerchantRoles(int page, int size, String name, java.util.UUID orgUnitId, java.util.List<java.util.UUID> authorizedOrgUnitIds);
    RoleResponse getRoleById(UUID roleId, List<UUID> authorizedOrgUnitIds);

}
