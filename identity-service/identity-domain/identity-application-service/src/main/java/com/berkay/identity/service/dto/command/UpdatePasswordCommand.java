package com.berkay.identity.service.dto.command;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class UpdatePasswordCommand {
    @NotNull
    private final String newPassword;
}
