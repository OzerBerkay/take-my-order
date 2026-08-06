package com.berkay.identity.service.dto.command;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class LoginCommand {
    @NotNull
    private final String email;
    @NotNull
    private final String password;
}
