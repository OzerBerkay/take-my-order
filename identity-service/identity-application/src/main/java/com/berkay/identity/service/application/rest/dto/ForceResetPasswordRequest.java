package com.berkay.identity.service.application.rest.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ForceResetPasswordRequest {
    @NotBlank(message = "New password is required")
    private String newPassword;
}
