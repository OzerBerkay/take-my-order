package com.berkay.identity.service.dto.command;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class ForceResetPasswordCommand {
    @NotNull
    private final UUID userId;
    
    @NotNull
    private final String newPassword;
}
