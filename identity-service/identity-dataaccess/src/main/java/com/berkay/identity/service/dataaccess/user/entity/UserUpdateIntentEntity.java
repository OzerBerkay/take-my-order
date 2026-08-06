package com.berkay.identity.service.dataaccess.user.entity;

import com.berkay.identity.service.domain.valueobject.IntentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "user_update_intent")
@Entity
public class UserUpdateIntentEntity {

    @Id
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private IntentStatus status;

    @Column(name = "command_type", nullable = false)
    private String commandType;

    @Column(name = "old_snapshot", columnDefinition = "jsonb")
    private String oldSnapshot;

    @Column(name = "new_snapshot", columnDefinition = "jsonb")
    private String newSnapshot;

    @Column(name = "created_at", nullable = false)
    private ZonedDateTime createdAt;

    @Column(name = "locked_at")
    private ZonedDateTime lockedAt;

    @Column(name = "locked_by")
    private String lockedBy;

    @Version
    private Integer version;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserUpdateIntentEntity that = (UserUpdateIntentEntity) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
