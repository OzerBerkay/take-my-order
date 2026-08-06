package com.berkay.payment.service.domain.dto.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class PermissionPayload {
    private UUID id;
    private String code;
    private String domain;
    private Boolean isActive;
    private Boolean isRestricted;
    private String createdAt;
    private String updatedAt;
}
