package com.berkay.order.service.application.rest;

import com.berkay.order.service.application.job.RoleInitialSyncJob;
import com.berkay.order.service.domain.ports.output.repository.RoleRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping(value = "/management", produces = "application/json")
public class RoleManagementController {

    private final RoleInitialSyncJob roleInitialSyncJob;
    private final RoleRepository roleRepository;

    public RoleManagementController(RoleInitialSyncJob roleInitialSyncJob,
                                    RoleRepository roleRepository) {
        this.roleInitialSyncJob = roleInitialSyncJob;
        this.roleRepository = roleRepository;
    }

    @PostMapping("/sync-roles")
    @PreAuthorize("@orderAuthService.hasPermission(authentication, 'order_service_can_sync_roles')")
    public ResponseEntity<String> syncRoles() {
        log.warn("Manual role sync triggered. Truncating current roles and fetching from identity-service...");
        // role_permissions_replica uses ON DELETE CASCADE, so we can just delete roles
        roleRepository.deleteAll();
        
        roleInitialSyncJob.forceSyncRoles();
        
        return ResponseEntity.ok("Role sync completed successfully.");
    }
}
