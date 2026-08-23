package com.berkay.identity.service.application.rest;

import com.berkay.identity.service.dto.command.CreateUserResponse;
import com.berkay.identity.service.dto.command.RegisterInternalUserCommand;
import com.berkay.identity.service.dto.command.TokenRevocationResponse;
import com.berkay.identity.service.handler.AdminUserCommandHandler;
import com.berkay.identity.service.ports.input.service.UserApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import com.berkay.identity.service.application.security.jwt.JwtAuthenticationToken;
import com.berkay.identity.service.domain.valueobject.UserType;

@Slf4j
@RestController
@RequestMapping(value = "/admin/users", produces = "application/vnd.api.v1+json")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserApplicationService userApplicationService;
    private final AdminUserCommandHandler adminUserCommandHandler;

    @PostMapping
    @org.springframework.security.access.prepost.PreAuthorize("authentication.userType == 'INTERNAL' and @roleAuthService.hasPermission(authentication, 'can_manage_system_settings')")
    public ResponseEntity<CreateUserResponse> registerInternalUser(@RequestBody @Valid RegisterInternalUserCommand command) {
        log.info("Received register internal user request for email: {}", command.getEmail());
        CreateUserResponse response = userApplicationService.registerInternalUser(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/{userId}/status")
    @org.springframework.security.access.prepost.PreAuthorize("authentication.userType == 'INTERNAL' and @roleAuthService.hasPermission(authentication, 'can_update_user_status')")
    public ResponseEntity<com.berkay.identity.service.dto.command.UpdateUserStatusResponse> updateUserStatus(
            @PathVariable("userId") java.util.UUID userId,
            @RequestBody @Valid com.berkay.identity.service.application.rest.dto.UpdateUserStatusRequest request) {
        log.info("Received PATCH request to update user status for user: {}", userId);
        
        com.berkay.identity.service.dto.command.UpdateUserStatusCommand effectiveCommand = 
                new com.berkay.identity.service.dto.command.UpdateUserStatusCommand(userId, request.getStatus());
                
        com.berkay.identity.service.dto.command.UpdateUserStatusResponse response = userApplicationService.updateUserStatus(effectiveCommand);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{userId}/password")
    @org.springframework.security.access.prepost.PreAuthorize("authentication.userType == 'INTERNAL' and @roleAuthService.hasPermission(authentication, 'can_reset_password')")
    public ResponseEntity<Void> forceResetPassword(
            @PathVariable("userId") java.util.UUID userId,
            @RequestBody @Valid com.berkay.identity.service.application.rest.dto.ForceResetPasswordRequest request) {
        
        log.info("Received PUT request to force reset password for user: {}", userId);
        
        userApplicationService.forceResetPassword(
            com.berkay.identity.service.dto.command.ForceResetPasswordCommand.builder()
                .userId(userId)
                .newPassword(request.getNewPassword())
                .build()
        );
        
        return ResponseEntity.noContent().build();
    }



    @GetMapping
    @org.springframework.security.access.prepost.PreAuthorize("@roleAuthService.hasPermission(authentication, 'can_view_users')")
    public ResponseEntity<com.berkay.identity.service.dto.query.PageResult<com.berkay.identity.service.dto.query.UserResponse>> getAdminUsers(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "email", required = false) String email,
            @RequestParam(value = "firstName", required = false) String firstName,
            @RequestParam(value = "lastName", required = false) String lastName,
            @RequestParam(value = "status", required = false) com.berkay.identity.service.domain.valueobject.AccountStatus status,
            @RequestParam(value = "userType", required = false) UserType userType,
            @RequestParam(value = "orgUnitId", required = false) java.util.UUID orgUnitId,
            @RequestParam(value = "roleId", required = false) java.util.UUID roleId) {

        log.info("Received GET request for admin users with page: {}, size: {}", page, size);
        com.berkay.identity.service.dto.query.GetAdminUsersQuery query = com.berkay.identity.service.dto.query.GetAdminUsersQuery.builder()
                .page(page)
                .size(size)
                .email(email)
                .firstName(firstName)
                .lastName(lastName)
                .status(status)
                .userType(userType)
                .orgUnitId(orgUnitId)
                .roleId(roleId)
                .build();
        return ResponseEntity.ok(userApplicationService.getAdminUsers(query));
    }

    @GetMapping("/{userId}")
    @org.springframework.security.access.prepost.PreAuthorize("authentication.userType == 'INTERNAL' and @roleAuthService.hasPermission(authentication, 'can_view_users')")
    public ResponseEntity<com.berkay.identity.service.dto.query.UserResponse> getAdminUserById(@PathVariable("userId") java.util.UUID userId) {
        log.info("Received GET request for admin user details: {}", userId);
        return ResponseEntity.ok(userApplicationService.getAdminUserById(userId));
    }

    @PostMapping("/{userId}/roles/{roleId}")
    @org.springframework.security.access.prepost.PreAuthorize("authentication.userType == 'INTERNAL' and @roleAuthService.hasPermission(authentication, 'can_manage_system_settings')")
    public ResponseEntity<Void> assignRoleToUser(
            @PathVariable("userId") java.util.UUID userId,
            @PathVariable("roleId") java.util.UUID roleId) {
        
        log.info("Received POST request to assign role {} to user {}", roleId, userId);

        java.util.UUID requesterId = null;
        com.berkay.identity.service.domain.valueobject.UserType requesterUserType = null;
        java.util.List<java.util.UUID> requesterRoleIds = null;
        Object principal = SecurityContextHolder.getContext().getAuthentication();
        if (principal instanceof JwtAuthenticationToken jwtAuth) {
            requesterId = jwtAuth.getInternalId();
            requesterUserType = jwtAuth.getUserType();
            requesterRoleIds = jwtAuth.getRoleIds();
        }
        
        userApplicationService.assignRoleToUser(
                new com.berkay.identity.service.dto.command.AssignUserRoleCommand(userId, roleId, requesterId, requesterUserType, requesterRoleIds)
        );
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/{userId}/roles/{roleId}")
    @org.springframework.security.access.prepost.PreAuthorize("authentication.userType == 'INTERNAL' and @roleAuthService.hasPermission(authentication, 'can_manage_system_settings')")
    public ResponseEntity<Void> unassignRoleFromUser(
            @PathVariable("userId") java.util.UUID userId,
            @PathVariable("roleId") java.util.UUID roleId) {
        
        log.info("Received DELETE request to unassign role {} from user {}", roleId, userId);

        java.util.UUID requesterId = null;
        com.berkay.identity.service.domain.valueobject.UserType requesterUserType = null;
        java.util.List<java.util.UUID> requesterRoleIds = null;
        Object principal = SecurityContextHolder.getContext().getAuthentication();
        if (principal instanceof JwtAuthenticationToken jwtAuth) {
            requesterId = jwtAuth.getInternalId();
            requesterUserType = jwtAuth.getUserType();
            requesterRoleIds = jwtAuth.getRoleIds();
        }
        
        userApplicationService.unassignRoleFromUser(
                new com.berkay.identity.service.dto.command.UnassignUserRoleCommand(userId, roleId, requesterId, requesterUserType, requesterRoleIds)
        );
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{userId}/revoke-access")
    @org.springframework.security.access.prepost.PreAuthorize("authentication.userType == 'INTERNAL' and @roleAuthService.hasPermission(authentication, 'can_revoke_user')")
    public ResponseEntity<TokenRevocationResponse> revokeAccess(@PathVariable("userId") java.util.UUID userId) {
        log.info("Received POST request to revoke access for user: {}", userId);
        return ResponseEntity.ok(adminUserCommandHandler.revokeAccess(userId));
    }

    @PostMapping("/{userId}/revoke-all")
    @org.springframework.security.access.prepost.PreAuthorize("authentication.userType == 'INTERNAL' and @roleAuthService.hasPermission(authentication, 'can_revoke_user')")
    public ResponseEntity<TokenRevocationResponse> revokeAll(@PathVariable("userId") java.util.UUID userId) {
        log.info("Received POST request to revoke all tokens for user: {}", userId);
        return ResponseEntity.ok(adminUserCommandHandler.revokeAll(userId));
    }
}