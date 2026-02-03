package com.berkay.identity.service.dataaccess.outbox.entity;

import com.berkay.outbox.OutboxStatus;
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
@Table(name = "user_outbox")
@Entity
public class UserOutboxEntity {

    @Id
    private UUID id;

    @Column(nullable = false)
    private ZonedDateTime createdAt;

    private ZonedDateTime processedAt;

    @Column(nullable = false)
    private String type; // DomainEventType (USER_CREATED vs)

    @Column(nullable = false) // JSON Payload
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OutboxStatus outboxStatus;

    @Version
    private int version;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserOutboxEntity that = (UserOutboxEntity) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}