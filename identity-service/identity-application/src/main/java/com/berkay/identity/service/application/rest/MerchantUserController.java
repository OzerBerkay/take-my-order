package com.berkay.identity.service.application.rest;

import com.berkay.identity.service.ports.input.service.UserApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping(value = "/merchant/users", produces = "application/vnd.api.v1+json")
@RequiredArgsConstructor
public class MerchantUserController {

    private final UserApplicationService userApplicationService;


    @GetMapping
    @org.springframework.security.access.prepost.PreAuthorize("@roleAuthService.hasPermissionForOrg(authentication, #orgUnitId, 'can_view_merchant_users')")
    public ResponseEntity<com.berkay.identity.service.dto.query.PageResult<com.berkay.identity.service.dto.query.MerchantUserResponse>> getMerchantUsers(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "email", required = false) String email,
            @RequestParam(value = "firstName", required = false) String firstName,
            @RequestParam(required = false) String lastName,
            @RequestParam UUID orgUnitId,
            @RequestParam(required = false) UUID roleId) {

        log.info("Received GET request for merchant users with page: {}, size: {}", page, size);

        // Fetch authorized org unit ids from JWT
        java.util.List<UUID> authorizedOrgUnitIds = getAuthorizedOrgUnitIds();

        com.berkay.identity.service.dto.query.GetMerchantUsersQuery query = com.berkay.identity.service.dto.query.GetMerchantUsersQuery.builder()
                .page(page)
                .size(size)
                .email(email)
                .firstName(firstName)
                .lastName(lastName)
                .filterOrgUnitId(orgUnitId)
                .authorizedOrgUnitIds(authorizedOrgUnitIds)
                .roleId(roleId)
                .build();
        return ResponseEntity.ok(userApplicationService.getMerchantUsers(query));
    }

    @GetMapping("/{userId}")
    @org.springframework.security.access.prepost.PreAuthorize("@roleAuthService.hasPermissionForOrg(authentication, #orgUnitId, 'can_view_merchant_users')")
    public ResponseEntity<com.berkay.identity.service.dto.query.MerchantUserResponse> getMerchantUserById(
            @PathVariable("userId") UUID userId,
            @RequestParam(value = "orgUnitId", required = true) UUID orgUnitId) {
        log.info("Received GET request for merchant user details: {}", userId);
        java.util.List<UUID> authorizedOrgUnitIds = getAuthorizedOrgUnitIds();
        return ResponseEntity.ok(userApplicationService.getMerchantUserById(userId, orgUnitId, authorizedOrgUnitIds));
    }

    @PostMapping("/{userId}/roles/{roleId}")
    public ResponseEntity<Void> assignRoleToUser(
            @PathVariable("userId") UUID userId,
            @PathVariable("roleId") UUID roleId) {
        
        log.info("Received POST request to assign role {} to user {}", roleId, userId);
        
        UUID requesterId = null;
        com.berkay.identity.service.domain.valueobject.UserType requesterUserType = null;
        java.util.List<UUID> requesterRoleIds = null;
        Object principal = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (principal instanceof com.berkay.identity.service.application.security.jwt.JwtAuthenticationToken jwtAuth) {
            requesterId = jwtAuth.getInternalId();
            requesterUserType = jwtAuth.getUserType();
            requesterRoleIds = jwtAuth.getRoleIds();
        }
        
        userApplicationService.assignRoleToUser(
                new com.berkay.identity.service.dto.command.AssignUserRoleCommand(userId, roleId, requesterId, requesterUserType, requesterRoleIds)
        );
        return ResponseEntity.status(org.springframework.http.HttpStatus.CREATED).build();
    }

    @DeleteMapping("/{userId}/roles/{roleId}")
    public ResponseEntity<Void> unassignRoleFromUser(
            @PathVariable("userId") UUID userId,
            @PathVariable("roleId") UUID roleId) {
        
        log.info("Received DELETE request to unassign role {} from user {}", roleId, userId);
        
        UUID requesterId = null;
        com.berkay.identity.service.domain.valueobject.UserType requesterUserType = null;
        java.util.List<UUID> requesterRoleIds = null;
        Object principal = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (principal instanceof com.berkay.identity.service.application.security.jwt.JwtAuthenticationToken jwtAuth) {
            requesterId = jwtAuth.getInternalId();
            requesterUserType = jwtAuth.getUserType();
            requesterRoleIds = jwtAuth.getRoleIds();
        }
        
        userApplicationService.unassignRoleFromUser(
                new com.berkay.identity.service.dto.command.UnassignUserRoleCommand(userId, roleId, requesterId, requesterUserType, requesterRoleIds)
        );
        return ResponseEntity.ok().build();
    }

    private java.util.List<UUID> getAuthorizedOrgUnitIds() {
        Object principal = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (principal instanceof com.berkay.identity.service.application.security.jwt.JwtAuthenticationToken jwtAuth) {
            return jwtAuth.getOrganizationalUnitIds();
        }
        return java.util.Collections.emptyList();
    }
}
