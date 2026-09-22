package com.berkay.identity.service.application.rest.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateMerchantUserRolesRequest {
    @NotNull(message = "Role IDs cannot be null")
    private List<UUID> roleIds;
}
