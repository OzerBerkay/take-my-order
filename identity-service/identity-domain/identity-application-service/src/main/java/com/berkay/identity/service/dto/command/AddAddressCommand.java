package com.berkay.identity.service.dto.command;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class AddAddressCommand {
    @NotNull
    private final String name;
    @NotNull
    private final String street;
    @NotNull
    private final String city;
    @NotNull
    private final String postalCode;
    @NotNull
    private final String country;
}
