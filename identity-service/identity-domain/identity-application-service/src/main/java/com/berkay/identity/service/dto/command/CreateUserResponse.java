package com.berkay.identity.service.dto.command; // Command klasörüne aldık

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class CreateUserResponse {
    private final UUID userId;
    private final String message;
}