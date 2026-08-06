package com.berkay.identity.service.domain.entity;

import com.berkay.domain.entity.BaseEntity;
import com.berkay.domain.valueobject.BaseId;
import com.berkay.identity.service.domain.valueobject.IntentId;
import com.berkay.identity.service.domain.valueobject.IntentStatus;
import com.berkay.identity.service.domain.valueobject.UserId;

import java.time.ZonedDateTime;
import java.util.UUID;

public class UserUpdateIntent extends BaseEntity<BaseId<UUID>> {
    private final UserId userId;
    private IntentStatus status;
    private final String commandType;
    private final String oldSnapshot;
    private final String newSnapshot;
    private final ZonedDateTime createdAt;
    private ZonedDateTime lockedAt;
    private String lockedBy;

    private UserUpdateIntent(Builder builder) {
        super.setId(builder.intentId);
        this.userId = builder.userId;
        this.status = builder.status;
        this.commandType = builder.commandType;
        this.oldSnapshot = builder.oldSnapshot;
        this.newSnapshot = builder.newSnapshot;
        this.createdAt = builder.createdAt;
        this.lockedAt = builder.lockedAt;
        this.lockedBy = builder.lockedBy;
    }

    public static Builder builder() {
        return new Builder();
    }

    public void complete() {
        this.status = IntentStatus.COMPLETED;
    }

    public void markAsKeycloakDone() {
        this.status = IntentStatus.KEYCLOAK_DONE;
    }

    public void fail() {
        this.status = IntentStatus.FAILED;
    }

    public void discard() {
        this.status = IntentStatus.DISCARDED;
    }

    public UserId getUserId() {
        return userId;
    }

    public IntentStatus getStatus() {
        return status;
    }

    public String getCommandType() {
        return commandType;
    }

    public String getOldSnapshot() {
        return oldSnapshot;
    }

    public String getNewSnapshot() {
        return newSnapshot;
    }

    public ZonedDateTime getCreatedAt() {
        return createdAt;
    }

    public ZonedDateTime getLockedAt() {
        return lockedAt;
    }

    public String getLockedBy() {
        return lockedBy;
    }

    public static final class Builder {
        private IntentId intentId;
        private UserId userId;
        private IntentStatus status;
        private String commandType;
        private String oldSnapshot;
        private String newSnapshot;
        private ZonedDateTime createdAt;
        private ZonedDateTime lockedAt;
        private String lockedBy;

        private Builder() {
        }

        public static Builder builder() {
            return new Builder();
        }

        public Builder intentId(IntentId val) {
            intentId = val;
            return this;
        }

        public Builder userId(UserId val) {
            userId = val;
            return this;
        }

        public Builder status(IntentStatus val) {
            status = val;
            return this;
        }

        public Builder commandType(String val) {
            commandType = val;
            return this;
        }

        public Builder oldSnapshot(String val) { oldSnapshot = val; return this; }
        public Builder newSnapshot(String val) { newSnapshot = val; return this; }
        public Builder createdAt(ZonedDateTime val) { createdAt = val; return this; }
        public Builder lockedAt(ZonedDateTime val) { lockedAt = val; return this; }
        public Builder lockedBy(String val) { lockedBy = val; return this; }

        public UserUpdateIntent build() { return new UserUpdateIntent(this); }
    }
}
