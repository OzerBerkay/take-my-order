package com.berkay.identity.service.application.rest;

import com.berkay.identity.service.application.rest.dto.UpdatePermissionRequest;
import com.berkay.identity.service.dto.command.permission.UpdatePermissionCommand;
import com.berkay.identity.service.dto.command.permission.UpdatePermissionResponse;
import com.berkay.identity.service.ports.input.service.PermissionApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping(value = "/permissions", produces = "application/vnd.api.v1+json")
@RequiredArgsConstructor
public class PermissionController {

    private final PermissionApplicationService permissionApplicationService;

    @PutMapping("/{permissionId}")
    public ResponseEntity<UpdatePermissionResponse> updatePermission(@PathVariable UUID permissionId,
                                                                     @RequestBody @Valid UpdatePermissionRequest request) {
        log.info("Received update permission request for permission id: {}", permissionId);
        
        UpdatePermissionCommand finalCommand = UpdatePermissionCommand.builder()
                .permissionId(permissionId)
                .description(request.getDescription())
                .active(request.getActive())
                .build();

        UpdatePermissionResponse response = permissionApplicationService.updatePermission(finalCommand);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @org.springframework.security.access.prepost.PreAuthorize("@roleAuthService.hasPermission(authentication, 'can_view_permissions')")
    public ResponseEntity<com.berkay.identity.service.dto.query.PageResult<com.berkay.identity.service.dto.query.PermissionResponse>> getPermissions(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        
        log.info("Received GET request for permissions");
        
        Object principal = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (principal instanceof com.berkay.identity.service.application.security.jwt.JwtAuthenticationToken jwtAuth) {
            if (jwtAuth.getUserType() == com.berkay.identity.service.domain.valueobject.UserType.MERCHANT) {
                return ResponseEntity.ok(permissionApplicationService.getMerchantPermissions(page, size));
            } else {
                return ResponseEntity.ok(permissionApplicationService.getAdminPermissions(page, size));
            }
        }
        throw new com.berkay.identity.service.domain.exception.IdentityDomainException("Could not extract user type from security context");
    }

    @GetMapping("/grouped")
    @org.springframework.security.access.prepost.PreAuthorize("@roleAuthService.hasPermission(authentication, 'can_view_permissions')")
    public ResponseEntity<java.util.Map<String, java.util.List<com.berkay.identity.service.dto.query.PermissionResponse>>> getGroupedPermissions() {
        log.info("Received GET request for grouped permissions");
        
        Object principal = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (principal instanceof com.berkay.identity.service.application.security.jwt.JwtAuthenticationToken jwtAuth) {
            boolean isAdmin = jwtAuth.getUserType() == com.berkay.identity.service.domain.valueobject.UserType.INTERNAL;
            return ResponseEntity.ok(permissionApplicationService.getGroupedPermissions(isAdmin));
        }
        throw new com.berkay.identity.service.domain.exception.IdentityDomainException("Could not extract user type from security context");
    }
}
