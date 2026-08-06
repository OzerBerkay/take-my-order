package com.berkay.identity.service.application.rest.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdatePermissionRequest {
    @NotNull(message = "Description is required")
    private String description;

    @NotNull(message = "Active flag is required")
    private Boolean active;
}
