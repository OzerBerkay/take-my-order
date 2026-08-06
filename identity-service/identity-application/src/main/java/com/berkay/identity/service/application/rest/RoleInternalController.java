package com.berkay.identity.service.application.rest;

import com.berkay.identity.service.domain.dto.role.SyncRolesQuery;
import com.berkay.identity.service.domain.dto.role.SyncRolesResponse;
import com.berkay.identity.service.ports.input.service.RoleApplicationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.ZonedDateTime;

@Slf4j
@RestController
@RequestMapping(value = "/internal/sync/roles", produces = "application/json")
public class RoleInternalController {

    private final RoleApplicationService roleApplicationService;

    public RoleInternalController(RoleApplicationService roleApplicationService) {
        this.roleApplicationService = roleApplicationService;
    }

    @GetMapping
    public ResponseEntity<SyncRolesResponse> syncRoles(
            @RequestParam(value = "cursor", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime cursor,
            @RequestParam(value = "limit", required = false, defaultValue = "100") int limit) {

        log.info("Received internal sync roles request with cursor: {} and limit: {}", cursor, limit);
        SyncRolesQuery query = SyncRolesQuery.builder()
                .cursor(cursor)
                .limit(limit)
                .build();

        SyncRolesResponse response = roleApplicationService.syncRoles(query);
        log.info("Returning {} roles for sync", response.getRoles().size());
        return ResponseEntity.ok(response);
    }
}
