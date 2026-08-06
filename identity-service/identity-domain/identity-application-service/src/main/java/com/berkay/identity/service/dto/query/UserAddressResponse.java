package com.berkay.identity.service.dto.query;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class UserAddressResponse {
    private final UUID id;
    private final UUID userId;
    private final String name;
    private final String street;
    private final String postalCode;
    private final String city;
    private final String country;
}
