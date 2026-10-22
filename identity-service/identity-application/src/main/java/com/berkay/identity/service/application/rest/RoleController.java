package com.berkay.identity.service.application.rest;

import com.berkay.identity.service.dto.command.role.*;
import com.berkay.identity.service.ports.input.service.RoleApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

import com.berkay.identity.service.application.security.auth.RoleAuthorizationService;

@Slf4j
@RestController
@RequestMapping(value = "/roles", produces = "application/vnd.api.v1+json")
@RequiredArgsConstructor
public class RoleController {

    private final RoleApplicationService roleApplicationService;
    private final RoleAuthorizationService roleAuthService;

    @PostMapping
    @PreAuthorize("@roleAuthService.canCreateRole(authentication, #organizationalUnitId != null ? #organizationalUnitId : #command.organizationalUnitId)")
    public ResponseEntity<CreateRoleResponse> createRole(@RequestParam(required = false) UUID organizationalUnitId,
                                                         @RequestBody @Valid CreateRoleCommand command) {
        log.info("Received create role request for role name: {}", command.getName());

        UUID effectiveOrgUnitId = organizationalUnitId != null ? organizationalUnitId : command.getOrganizationalUnitId();

        CreateRoleCommand finalCommand = CreateRoleCommand.builder()
                .name(command.getName())
                .permissionIds(command.getPermissionIds())
                .organizationalUnitId(effectiveOrgUnitId)
                .build();

        CreateRoleResponse response = roleApplicationService.createRole(finalCommand);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{roleId}")
    @PreAuthorize("@roleAuthService.canUpdateRole(authentication, #roleId)")
    public ResponseEntity<UpdateRoleResponse> updateRole(@PathVariable UUID roleId,
                                                         @RequestParam(required = false) UUID organizationalUnitId,
                                                         @RequestBody @Valid UpdateRoleCommand command) {
        log.info("Received update role request for role id: {}", roleId);

        UUID effectiveOrgUnitId = organizationalUnitId != null ? organizationalUnitId : command.getOrganizationalUnitId();

        UpdateRoleCommand finalCommand = UpdateRoleCommand.builder()
                .roleId(roleId)
                .name(command.getName())
                .permissionIds(command.getPermissionIds())
                .organizationalUnitId(effectiveOrgUnitId)
                .build();

        UpdateRoleResponse response = roleApplicationService.updateRole(finalCommand);
        roleAuthService.evictRoleCache(roleId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{roleId}")
    @PreAuthorize("@roleAuthService.canDeleteRole(authentication, #roleId)")
    public ResponseEntity<DeleteRoleResponse> deleteRole(@PathVariable UUID roleId,
                                                         @RequestParam(required = false) UUID organizationalUnitId) {
        log.info("Received delete role request for role id: {}", roleId);

        DeleteRoleCommand finalCommand = DeleteRoleCommand.builder()
                .roleId(roleId)
                .organizationalUnitId(organizationalUnitId)
                .build();

        DeleteRoleResponse response = roleApplicationService.deleteRole(finalCommand);
        roleAuthService.evictRoleCache(roleId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<com.berkay.identity.service.dto.query.PageResult<com.berkay.identity.service.dto.query.RoleResponse>> getRoles(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "orgUnitId", required = false) UUID orgUnitId,
            @RequestParam(value = "userType", required = false) String userType) {

        log.info("Received GET request for roles");

        Object principal = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (principal instanceof com.berkay.identity.service.application.security.jwt.JwtAuthenticationToken jwtAuth) {
            if (jwtAuth.getUserType() == com.berkay.identity.service.domain.valueobject.UserType.MERCHANT) {
                if (orgUnitId == null) {
                    throw new com.berkay.identity.service.domain.exception.IdentityDomainException("orgUnitId is required for merchant users to fetch roles.");
                }
                if (!roleAuthService.hasPermissionForOrg(jwtAuth, orgUnitId, "can_view_roles")) {
                    throw new org.springframework.security.access.AccessDeniedException("Access Denied: Missing can_view_roles permission for the specified organizational unit.");
                }
                return ResponseEntity.ok(roleApplicationService.getMerchantRoles(page, size, name, orgUnitId, jwtAuth.getOrganizationalUnitIds()));
            } else {
                if (!roleAuthService.hasPermission(jwtAuth, "can_view_roles")) {
                    throw new org.springframework.security.access.AccessDeniedException("Access Denied: Missing can_view_roles permission.");
                }
                return ResponseEntity.ok(roleApplicationService.getAdminRoles(page, size, name, orgUnitId, userType));
            }
        }
        throw new com.berkay.identity.service.domain.exception.IdentityDomainException("Could not extract user type from security context");
    }

    @GetMapping("/{roleId}")
    @PreAuthorize("@roleAuthService.hasPermission(authentication, 'can_view_roles')")
    public ResponseEntity<com.berkay.identity.service.dto.query.RoleResponse> getRoleById(@PathVariable("roleId") UUID roleId) {
        log.info("Received GET request for role details: {}", roleId);
        
        Object principal = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (principal instanceof com.berkay.identity.service.application.security.jwt.JwtAuthenticationToken jwtAuth) {
            com.berkay.identity.service.dto.query.RoleResponse response = roleApplicationService.getRoleById(roleId, null);
            if (jwtAuth.getUserType() == com.berkay.identity.service.domain.valueobject.UserType.MERCHANT) {
                if (response.getOrganizationalUnitId() == null || !roleAuthService.hasPermissionForOrg(jwtAuth, response.getOrganizationalUnitId(), "can_view_roles")) {
                    throw new org.springframework.security.access.AccessDeniedException("Access Denied: Missing can_view_roles permission for this role's organizational unit.");
                }
            }
            return ResponseEntity.ok(response);
        }
        throw new com.berkay.identity.service.domain.exception.IdentityDomainException("Could not extract user type from security context");
    }
}