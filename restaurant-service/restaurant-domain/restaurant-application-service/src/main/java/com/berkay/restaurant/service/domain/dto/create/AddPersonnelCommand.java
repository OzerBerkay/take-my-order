package com.berkay.restaurant.service.domain.dto.create;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class AddPersonnelCommand {
    @NotNull(message = "Restaurant ID is required")
    private final UUID restaurantId;

    @NotNull(message = "Added by Merchant ID is required")
    private final UUID addedByMerchantId;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private final String email;
}
