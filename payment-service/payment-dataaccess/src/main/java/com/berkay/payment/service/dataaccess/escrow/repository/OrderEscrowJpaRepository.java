package com.berkay.payment.service.dataaccess.escrow.repository;

import com.berkay.payment.service.dataaccess.escrow.entity.OrderEscrowEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderEscrowJpaRepository extends JpaRepository<OrderEscrowEntity, UUID> {

    Optional<OrderEscrowEntity> findByOrderId(UUID orderId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT e FROM OrderEscrowEntity e WHERE e.orderId = :orderId")
    Optional<OrderEscrowEntity> findByOrderIdWithLock(@Param("orderId") UUID orderId);

    @Query(value = """
            SELECT
                COALESCE(SUM(amount) FILTER (WHERE status = 'RELEASED' AND updated_at >= CAST(:startOfDay AS TIMESTAMPTZ)), 0.00) AS "dailySettledRevenue",
                COALESCE(SUM(amount) FILTER (WHERE status = 'HOLDING'), 0.00) AS "pendingPayout",
                COALESCE(SUM(amount) FILTER (WHERE status = 'REFUNDED' AND updated_at >= CAST(:startOfDay AS TIMESTAMPTZ)), 0.00) AS "dailyRefundedAmount"
            FROM payment.order_escrows
            WHERE restaurant_id = :restaurantId
            """, nativeQuery = true)
    com.berkay.payment.service.domain.ports.output.repository.OrderEscrowSummaryProjection getEscrowSummary(
            @Param("restaurantId") UUID restaurantId,
            @Param("startOfDay") java.time.OffsetDateTime startOfDay);
}
