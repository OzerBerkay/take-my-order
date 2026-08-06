package com.berkay.identity.service.dto.command;

import com.berkay.identity.service.domain.valueobject.AccountStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class UpdateUserStatusCommand {
    @NotNull
    private final UUID targetUserId;
    @NotNull
    private final AccountStatus status;
}
