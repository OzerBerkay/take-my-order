package com.berkay.identity.service.dto.command;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class VerifyPhoneCommand {
    @NotBlank
    private final String phoneNumber;
    @NotBlank
    private final String code;
}