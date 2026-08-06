package com.berkay.restaurant.service.domain.dto.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class PermissionEventPayload {
    private final String eventType;
    private final PermissionPayload permission;
}
