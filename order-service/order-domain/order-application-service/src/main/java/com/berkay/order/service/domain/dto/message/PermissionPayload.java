package com.berkay.order.service.domain.dto.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
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
