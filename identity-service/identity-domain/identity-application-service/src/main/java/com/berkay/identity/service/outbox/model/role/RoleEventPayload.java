package com.berkay.identity.service.outbox.model.role;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;


@Getter
@Builder
@Jacksonized
@AllArgsConstructor
public class RoleEventPayload {
    private final String eventType; // ROLE_CREATED, ROLE_UPDATED, ROLE_DELETED

    private final RolePayload role;
}