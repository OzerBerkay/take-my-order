package com.berkay.identity.service;

import com.berkay.identity.service.dto.command.role.*;
import com.berkay.identity.service.handler.role.CreateRoleCommandHandler;
import com.berkay.identity.service.handler.role.DeleteRoleCommandHandler;
import com.berkay.identity.service.handler.role.SyncRolesQueryHandler;
import com.berkay.identity.service.handler.role.UpdateRoleCommandHandler;
import com.berkay.identity.service.ports.input.service.RoleApplicationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Slf4j
@Validated
@Service
public class RoleApplicationServiceImpl implements RoleApplicationService {

    private final CreateRoleCommandHandler createRoleCommandHandler;
    private final UpdateRoleCommandHandler updateRoleCommandHandler;
    private final DeleteRoleCommandHandler deleteRoleCommandHandler;
    private final SyncRolesQueryHandler syncRolesQueryHandler;
    private final com.berkay.identity.service.ports.output.repository.RoleQueryRepository roleQueryRepository;

    public RoleApplicationServiceImpl(CreateRoleCommandHandler createRoleCommandHandler,
                                      UpdateRoleCommandHandler updateRoleCommandHandler,
                                      DeleteRoleCommandHandler deleteRoleCommandHandler,
                                      SyncRolesQueryHandler syncRolesQueryHandler,
                                      com.berkay.identity.service.ports.output.repository.RoleQueryRepository roleQueryRepository) {
        this.createRoleCommandHandler = createRoleCommandHandler;
        this.updateRoleCommandHandler = updateRoleCommandHandler;
        this.deleteRoleCommandHandler = deleteRoleCommandHandler;
        this.syncRolesQueryHandler = syncRolesQueryHandler;
        this.roleQueryRepository = roleQueryRepository;
    }

    @Override
    public CreateRoleResponse createRole(CreateRoleCommand command) {
        return createRoleCommandHandler.createRole(command);
    }

    @Override
    public UpdateRoleResponse updateRole(UpdateRoleCommand command) {
        return updateRoleCommandHandler.updateRole(command);
    }

    @Override
    public DeleteRoleResponse deleteRole(DeleteRoleCommand command) {
        return deleteRoleCommandHandler.deleteRole(command);
    }

    @Override
    public com.berkay.identity.service.domain.dto.role.SyncRolesResponse syncRoles(com.berkay.identity.service.domain.dto.role.SyncRolesQuery query) {
        return syncRolesQueryHandler.syncRoles(query);
    }

    @Override
    public com.berkay.identity.service.dto.query.PageResult<com.berkay.identity.service.dto.query.RoleResponse> getAdminRoles(int page, int size, String name, java.util.UUID orgUnitId, String userType) {
        return roleQueryRepository.getAdminRoles(page, size, name, orgUnitId, userType);
    }

    @Override
    public com.berkay.identity.service.dto.query.PageResult<com.berkay.identity.service.dto.query.RoleResponse> getMerchantRoles(int page, int size, String name, java.util.UUID orgUnitId, java.util.List<java.util.UUID> authorizedOrgUnitIds) {
        return roleQueryRepository.getMerchantRoles(page, size, name, orgUnitId, authorizedOrgUnitIds);
    }

    @Override
    public com.berkay.identity.service.dto.query.RoleResponse getRoleById(java.util.UUID roleId, java.util.List<java.util.UUID> authorizedOrgUnitIds) {
        return roleQueryRepository.getRoleById(roleId, authorizedOrgUnitIds);
    }


}