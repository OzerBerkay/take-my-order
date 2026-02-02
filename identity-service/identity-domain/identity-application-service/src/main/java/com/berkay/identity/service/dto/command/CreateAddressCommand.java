package com.berkay.identity.service.dto.command;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class CreateAddressCommand {
    @NotBlank(message = "Address name is required (e.g. Home, Work)")
    @Size(max = 50)
    private final String name;

    @NotBlank(message = "Street is required")
    @Size(max = 255)
    private final String street;

    @NotBlank(message = "City is required")
    @Size(max = 50)
    private final String city;

    @NotBlank(message = "Postal code is required")
    @Size(max = 10)
    private final String postalCode;

    @NotBlank(message = "Country is required")
    @Size(max = 50)
    private final String country;
}