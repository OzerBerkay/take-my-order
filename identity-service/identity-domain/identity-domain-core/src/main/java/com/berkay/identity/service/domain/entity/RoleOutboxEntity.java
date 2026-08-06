package com.berkay.identity.service.domain.entity;

import com.berkay.domain.entity.BaseEntity;
import com.berkay.domain.valueobject.BaseId;

import java.time.ZonedDateTime;
import java.util.UUID;

public class RoleOutboxEntity extends BaseEntity<BaseId<UUID>> {
    private final String eventType;
    private final String payload;
    private String outboxStatus; // STARTED, COMPLETED, FAILED
    private int retryCount;
    private final ZonedDateTime createdAt;
    private ZonedDateTime processedAt;
    private final Long version; // Rol version for duplicate checking

    private RoleOutboxEntity(Builder builder) {
        super.setId(builder.outboxId);
        this.eventType = builder.eventType;
        this.payload = builder.payload;
        this.outboxStatus = builder.outboxStatus;
        this.retryCount = builder.retryCount;
        this.createdAt = builder.createdAt;
        this.processedAt = builder.processedAt;
        this.version = builder.version;
    }

    public static Builder builder() {
        return new Builder();
    }

    public void complete(ZonedDateTime processedAt) {
        this.outboxStatus = "COMPLETED";
        this.processedAt = processedAt;
    }

    public void fail() {
        this.outboxStatus = "FAILED";
    }

    public void incrementRetryCount() {
        this.retryCount++;
    }

    public String getEventType() { return eventType; }
    public String getPayload() { return payload; }
    public String getOutboxStatus() { return outboxStatus; }
    public int getRetryCount() { return retryCount; }
    public ZonedDateTime getCreatedAt() { return createdAt; }
    public ZonedDateTime getProcessedAt() { return processedAt; }
    public Long getVersion() { return version; }

    public static final class Builder {
        private BaseId<UUID> outboxId;
        private String eventType;
        private String payload;
        private String outboxStatus;
        private int retryCount;
        private ZonedDateTime createdAt;
        private ZonedDateTime processedAt;
        private Long version;

        public Builder outboxId(BaseId<UUID> val) { outboxId = val; return this; }
        public Builder eventType(String val) { eventType = val; return this; }
        public Builder payload(String val) { payload = val; return this; }
        public Builder outboxStatus(String val) { outboxStatus = val; return this; }
        public Builder retryCount(int val) { retryCount = val; return this; }
        public Builder createdAt(ZonedDateTime val) { createdAt = val; return this; }
        public Builder processedAt(ZonedDateTime val) { processedAt = val; return this; }
        public Builder version(Long val) { version = val; return this; }

        public RoleOutboxEntity build() { return new RoleOutboxEntity(this); }
    }
}
