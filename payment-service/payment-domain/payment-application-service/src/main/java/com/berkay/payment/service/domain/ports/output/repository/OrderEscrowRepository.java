package com.berkay.payment.service.domain.ports.output.repository;

import com.berkay.domain.valueobject.OrderId;
import com.berkay.payment.service.domain.entity.OrderEscrow;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

public interface OrderEscrowRepository {
    OrderEscrow save(OrderEscrow orderEscrow);
    Optional<OrderEscrow> findByOrderId(OrderId orderId);
    Optional<OrderEscrow> findByOrderIdWithLock(OrderId orderId);
    OrderEscrowSummaryProjection getEscrowSummary(UUID restaurantId, OffsetDateTime startOfDay);
}
