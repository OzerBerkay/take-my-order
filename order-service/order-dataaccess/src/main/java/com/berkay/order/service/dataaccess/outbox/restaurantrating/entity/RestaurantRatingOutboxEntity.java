package com.berkay.order.service.dataaccess.outbox.restaurantrating.entity;

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
@Table(name = "restaurant_rating_outbox")
@Entity
public class RestaurantRatingOutboxEntity {

    @Id
    private UUID id;

    @Column(name = "restaurant_id", nullable = false)
    private UUID restaurantId;

    @Column(name = "created_at", nullable = false)
    private ZonedDateTime createdAt;

    @Column(name = "processed_at")
    private ZonedDateTime processedAt;

    @Column(nullable = false, length = 50)
    private String type;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(name = "outbox_status", nullable = false, length = 50)
    private OutboxStatus outboxStatus;

    @Version
    private int version;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RestaurantRatingOutboxEntity that = (RestaurantRatingOutboxEntity) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
