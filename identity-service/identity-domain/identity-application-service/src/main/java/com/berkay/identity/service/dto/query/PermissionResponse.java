package com.berkay.identity.service.dto.query;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class PermissionResponse {
    private final UUID id;
    private final String name;
    private final String description;
    private final boolean active;
    private final boolean isRestricted;
}
