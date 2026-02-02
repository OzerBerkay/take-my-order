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
public class RegisterCustomerCommand {

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private final String email;

    @NotBlank(message = "Password is required") // domain katmanına inmeyecek sadece keycloak'a göndermek için gerekli
    @Size(min = 8, max = 40)
    private final String password;

    @NotBlank(message = "First name is required")
    @Size(max = 50)
    private final String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 50)
    private final String lastName;

    @NotBlank(message = "Phone number is required")
    @Size(min = 10, max = 15) // +90... formatı için
    private final String phoneNumber;

    // Adres listesi opsiyonel olabilir (null gelebilir) ama gelirse içi valid olmalı.
    @Valid
    private final List<CreateAddressCommand> addresses;
}