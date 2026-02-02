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

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 40)
    private final String password;

    @NotBlank(message = "First name is required")
    @Size(max = 50)
    private final String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 50)
    private final String lastName;

    @NotBlank(message = "Phone number is required")
    @Size(min = 10, max = 15)
    private final String phoneNumber;

    @Valid
    private final List<CreateAddressCommand> addresses;
}