package com.berkay.identity.service.dto.command;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class AddressResponse {
    private final UUID addressId;
    private final String message;
}
