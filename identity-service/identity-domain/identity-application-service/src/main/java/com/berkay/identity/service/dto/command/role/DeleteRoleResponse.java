package com.berkay.identity.service.dto.command.role;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class DeleteRoleResponse {
    private final UUID roleId;
}