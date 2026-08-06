package com.berkay.identity.service.application.rest.dto;

import com.berkay.identity.service.domain.valueobject.AccountStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserStatusRequest {
    @NotNull(message = "Status cannot be null")
    private AccountStatus status;
}
