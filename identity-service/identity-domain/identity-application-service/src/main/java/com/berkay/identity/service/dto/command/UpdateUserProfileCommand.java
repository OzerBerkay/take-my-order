package com.berkay.identity.service.dto.command;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import jakarta.validation.constraints.NotBlank;

@Getter
@Builder
@AllArgsConstructor
public class UpdateUserProfileCommand {
    
    @NotBlank(message = "First name must not be blank")
    private final String firstName;
    
    @NotBlank(message = "Last name must not be blank")
    private final String lastName;
    private final String imageUrl;
}

