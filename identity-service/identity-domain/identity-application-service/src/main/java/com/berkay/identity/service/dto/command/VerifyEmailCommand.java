package com.berkay.identity.service.dto.command;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class VerifyEmailCommand {
    @NotBlank
    private final String email;
    @NotBlank
    private final String code; // Email'e gelen link token'ı
}

