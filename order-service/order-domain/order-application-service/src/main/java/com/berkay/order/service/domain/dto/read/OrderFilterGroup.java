package com.berkay.order.service.domain.dto.read;

import com.berkay.domain.valueobject.OrderStatus;

import java.util.List;

public enum OrderFilterGroup {
    ALL(List.of()),
    ACTIVE(List.of(
            OrderStatus.PENDING,
            OrderStatus.PAID,
            OrderStatus.WAITING_FOR_APPROVAL,
            OrderStatus.APPROVED,
            OrderStatus.PREPARING,
            OrderStatus.READY_FOR_PICKUP,
            OrderStatus.IN_DELIVERY
    )),
    COMPLETED(List.of(OrderStatus.DELIVERED)),
    CANCELLED(List.of(OrderStatus.CANCELLING, OrderStatus.CANCELLED));

    private final List<OrderStatus> statuses;

    OrderFilterGroup(List<OrderStatus> statuses) {
        this.statuses = statuses;
    }

    public List<OrderStatus> getStatuses() {
        return statuses;
    }
}
