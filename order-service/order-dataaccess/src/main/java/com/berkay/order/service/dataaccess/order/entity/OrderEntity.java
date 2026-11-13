package com.berkay.order.service.dataaccess.order.entity;

import com.berkay.domain.valueobject.CancelReason;
import com.berkay.domain.valueobject.MaterialAction;
import com.berkay.domain.valueobject.OrderStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "orders")
@Entity
public class OrderEntity {

    @Id
    private UUID id;
    private UUID customerId;
    private UUID restaurantId;
    private UUID trackingId;
    private BigDecimal price;
    private BigDecimal deliveryFee;
    private java.time.ZonedDateTime createdAt;
    private java.time.ZonedDateTime deliveredAt;
    private boolean isReviewed;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_status")
    private OrderStatus orderStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "cancel_reason")
    private CancelReason cancelReason;

    @Enumerated(EnumType.STRING)
    @Column(name = "material_action")
    private MaterialAction materialAction;

    @Column(name = "internal_note")
    private String internalNote;

    @Column(name = "idempotency_key", unique = true, nullable = false)
    private String idempotencyKey;

    @Version
    private Integer version;

    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL)
    private OrderAddressEntity address;

    @org.hibernate.annotations.BatchSize(size = 20)
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderItemEntity> items;

    @PrePersist
    public void prePersist() {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            idempotencyKey = "legacy-" + (id != null ? id.toString() : UUID.randomUUID().toString());
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrderEntity that = (OrderEntity) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
