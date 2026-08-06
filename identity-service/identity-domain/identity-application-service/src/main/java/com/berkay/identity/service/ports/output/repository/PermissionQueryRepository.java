package com.berkay.identity.service.ports.output.repository;

import com.berkay.identity.service.dto.query.PageResult;
import com.berkay.identity.service.dto.query.PermissionResponse;

import java.util.List;
import java.util.Map;

public interface PermissionQueryRepository {
    PageResult<PermissionResponse> getAdminPermissions(int page, int size);
    PageResult<PermissionResponse> getMerchantPermissions(int page, int size);
    Map<String, List<PermissionResponse>> getGroupedPermissions(boolean isAdmin);
}
