package com.berkay.order.service.dataaccess.outbox.delivery.entity;

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
@Table(name = "delivery_outbox")
@Entity
public class DeliveryOutboxEntity {

    @Id
    private UUID id;

    @Column(name = "order_id", nullable = false)
    private UUID orderId;

    @Column(name = "created_at", nullable = false)
    private ZonedDateTime createdAt;

    @Column(name = "processed_at")
    private ZonedDateTime processedAt;

    @Column(nullable = false)
    private String type;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(name = "outbox_status", nullable = false)
    private OutboxStatus outboxStatus;

    @Version
    private int version;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DeliveryOutboxEntity that = (DeliveryOutboxEntity) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
