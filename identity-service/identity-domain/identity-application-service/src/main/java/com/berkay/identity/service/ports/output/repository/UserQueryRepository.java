package com.berkay.identity.service.ports.output.repository;

import com.berkay.identity.service.dto.query.GetAdminUsersQuery;
import com.berkay.identity.service.dto.query.GetMerchantUsersQuery;
import com.berkay.identity.service.dto.query.PageResult;
import com.berkay.identity.service.dto.query.UserResponse;

import java.util.List;
import java.util.UUID;

import com.berkay.identity.service.dto.query.MerchantUserResponse;

public interface UserQueryRepository {
    PageResult<UserResponse> getAdminUsers(GetAdminUsersQuery query);
    PageResult<MerchantUserResponse> getMerchantUsers(GetMerchantUsersQuery query);
    UserResponse getAdminUserById(UUID userId);
    MerchantUserResponse getMerchantUserById(UUID userId, UUID orgUnitId, List<UUID> authorizedOrgUnitIds);
}
