package com.berkay.identity.service;

import com.berkay.identity.service.dto.command.permission.UpdatePermissionCommand;
import com.berkay.identity.service.dto.command.permission.UpdatePermissionResponse;
import com.berkay.identity.service.handler.permission.UpdatePermissionCommandHandler;
import com.berkay.identity.service.ports.input.service.PermissionApplicationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Slf4j
@Validated
@Service
@RequiredArgsConstructor
public class PermissionApplicationServiceImpl implements PermissionApplicationService {

    private final UpdatePermissionCommandHandler updatePermissionCommandHandler;
    private final com.berkay.identity.service.ports.output.repository.PermissionQueryRepository permissionQueryRepository;

    @Override
    public UpdatePermissionResponse updatePermission(UpdatePermissionCommand command) {
        return updatePermissionCommandHandler.updatePermission(command);
    }

    @Override
    public com.berkay.identity.service.dto.query.PageResult<com.berkay.identity.service.dto.query.PermissionResponse> getAdminPermissions(int page, int size) {
        return permissionQueryRepository.getAdminPermissions(page, size);
    }

    @Override
    public com.berkay.identity.service.dto.query.PageResult<com.berkay.identity.service.dto.query.PermissionResponse> getMerchantPermissions(int page, int size) {
        return permissionQueryRepository.getMerchantPermissions(page, size);
    }

    @Override
    public java.util.Map<String, java.util.List<com.berkay.identity.service.dto.query.PermissionResponse>> getGroupedPermissions(boolean isAdmin) {
        return permissionQueryRepository.getGroupedPermissions(isAdmin);
    }
}
