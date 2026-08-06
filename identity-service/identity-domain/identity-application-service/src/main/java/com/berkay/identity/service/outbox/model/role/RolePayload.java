package com.berkay.identity.service.outbox.model.role;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

import java.util.List;
import java.util.UUID;

@Getter
@Builder
@Jacksonized
@AllArgsConstructor
public class RolePayload {
    private final UUID id;
    private final String name;
    private final String userType;
    private final UUID organizationalUnitId;
    private final Long version;
    private final List<PermissionPayload> permissions;
}