package com.berkay.identity.service.dto.command;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class TokenRevocationResponse {
    private final UUID userId;
    private final String message;
}
