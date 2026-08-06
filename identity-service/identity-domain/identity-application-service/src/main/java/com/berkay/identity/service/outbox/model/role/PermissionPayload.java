package com.berkay.identity.service.outbox.model.role;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

import java.util.UUID;

@Getter
@Builder
@Jacksonized
@AllArgsConstructor
public class PermissionPayload {
    private final UUID id;
    private final String code;
    private final String domain;
    private final Boolean isActive;
    private final Boolean isRestricted;
    private final String createdAt;
    private final String updatedAt;
}