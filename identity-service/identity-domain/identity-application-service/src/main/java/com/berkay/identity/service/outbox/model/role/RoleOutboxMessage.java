package com.berkay.identity.service.outbox.model.role;

import com.berkay.outbox.OutboxStatus; // Common modülündeki enum (STARTED, COMPLETED, FAILED)
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class RoleOutboxMessage {
    private final UUID id;
    private final ZonedDateTime createdAt;
    private final String type;
    private final String payload;
    @Setter
    private OutboxStatus outboxStatus;
    private ZonedDateTime processedAt;
    private final Long version;

    public void setProcessedAt(ZonedDateTime processedAt) { this.processedAt = processedAt; }
}