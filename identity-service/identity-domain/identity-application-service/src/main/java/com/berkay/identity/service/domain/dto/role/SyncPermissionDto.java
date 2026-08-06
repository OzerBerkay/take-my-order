package com.berkay.identity.service.domain.dto.role;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class SyncPermissionDto {
    private final UUID id;
    private final String code;
    private final String domain;
}
