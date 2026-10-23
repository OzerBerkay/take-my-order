package com.berkay.order.service.dataaccess.order.repository;

import com.berkay.order.service.dataaccess.order.entity.OrderEntity;
import com.berkay.order.service.domain.ports.output.repository.OrderAuthProjection;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;
import java.util.Optional;

public interface OrderJpaRepository extends JpaRepository<OrderEntity, UUID>,
        org.springframework.data.jpa.repository.JpaSpecificationExecutor<OrderEntity> {
    Optional<OrderEntity> findByTrackingId(UUID trackingId);

    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("SELECT o FROM OrderEntity o WHERE o.id = :id")
    Optional<OrderEntity> findByIdWithLock(@org.springframework.data.repository.query.Param("id") UUID id);

    @org.springframework.data.jpa.repository.Query("SELECT o.customerId AS customerId, o.restaurantId AS restaurantId, o.orderStatus AS orderStatus FROM OrderEntity o WHERE o.id = :id")
    Optional<OrderAuthProjection> findAuthProjectionById(@org.springframework.data.repository.query.Param("id") UUID id);

    org.springframework.data.domain.Page<OrderEntity> findByCustomerId(UUID customerId, org.springframework.data.domain.Pageable pageable);

    org.springframework.data.domain.Page<OrderEntity> findByCustomerIdAndOrderStatusIn(
            UUID customerId,
            java.util.Collection<com.berkay.domain.valueobject.OrderStatus> orderStatuses,
            org.springframework.data.domain.Pageable pageable);

    @org.springframework.data.jpa.repository.Query(value = """
            SELECT
                COUNT(*) AS "allCount",
                COUNT(*) FILTER (WHERE order_status IN ('PENDING', 'PAID', 'WAITING_FOR_APPROVAL', 'APPROVED', 'PREPARING', 'READY_FOR_PICKUP', 'IN_DELIVERY')) AS "activeCount",
                COUNT(*) FILTER (WHERE order_status = 'DELIVERED') AS "succeededCount",
                COUNT(*) FILTER (WHERE order_status IN ('CANCELLING', 'CANCELLED')) AS "abortCount"
            FROM "order".orders
            WHERE customer_id = :customerId
            """, nativeQuery = true)
    com.berkay.order.service.domain.ports.output.repository.CustomerOrdersSummaryProjection getCustomerOrdersSummary(
            @org.springframework.data.repository.query.Param("customerId") UUID customerId);
    
    Optional<java.util.List<OrderEntity>> findByRestaurantIdAndOrderStatus(UUID restaurantId, com.berkay.domain.valueobject.OrderStatus orderStatus);

    @org.springframework.data.jpa.repository.Query(value = """
            SELECT
                COUNT(*) FILTER (WHERE order_status NOT IN ('PENDING', 'PAID')) AS "totalCount",
                COUNT(*) FILTER (WHERE order_status = 'WAITING_FOR_APPROVAL') AS "waitingCount",
                COUNT(*) FILTER (WHERE order_status = 'APPROVED') AS "approvedCount",
                COUNT(*) FILTER (WHERE order_status = 'PREPARING') AS "preparingCount",
                COUNT(*) FILTER (WHERE order_status = 'READY_FOR_PICKUP') AS "readyCount",
                COUNT(*) FILTER (WHERE order_status = 'IN_DELIVERY') AS "inDeliveryCount",
                COUNT(*) FILTER (WHERE order_status = 'DELIVERED') AS "deliveredCount",
                COUNT(*) FILTER (WHERE order_status IN ('CANCELLING', 'CANCELLED')) AS "cancelledCount"
            FROM "order".orders
            WHERE restaurant_id = :restaurantId
              AND (cancel_reason IS NULL OR cancel_reason != 'PAYMENT_FAILED')
            """, nativeQuery = true)
    com.berkay.order.service.domain.ports.output.repository.RestaurantOrdersSummaryProjection getRestaurantOrdersSummary(
            @org.springframework.data.repository.query.Param("restaurantId") UUID restaurantId);
}
