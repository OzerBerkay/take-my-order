package com.berkay.identity.service.dto.command;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class RegisterMerchantCommand {

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private final String email;

    @com.berkay.identity.service.dto.validation.ValidPassword
    private final String password;

    @NotBlank(message = "First name is required")
    @Size(max = 50)
    private final String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 50)
    private final String lastName;

    @com.berkay.identity.service.dto.validation.ValidPhoneNumber
    private final String phoneNumber;

    @Valid
    private final List<CreateAddressCommand> addresses;
}