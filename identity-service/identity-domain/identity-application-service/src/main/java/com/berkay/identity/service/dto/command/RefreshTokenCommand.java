package com.berkay.identity.service.dto.command;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class RefreshTokenCommand {
    @NotNull
    private final String refreshToken;
}
