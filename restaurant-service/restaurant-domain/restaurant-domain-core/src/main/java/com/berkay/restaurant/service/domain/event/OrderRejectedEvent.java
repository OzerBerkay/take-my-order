package com.berkay.restaurant.service.domain.event;

import com.berkay.domain.valueobject.CancelReason;
import com.berkay.domain.valueobject.RestaurantId;
import com.berkay.restaurant.service.domain.entity.OrderApproval;

import java.time.ZonedDateTime;
import java.util.List;

public class OrderRejectedEvent extends OrderApprovalEvent {

    private final CancelReason cancelReason;

    public OrderRejectedEvent(OrderApproval orderApproval,
                              RestaurantId restaurantId,
                              List<String> failureMessages,
                              ZonedDateTime createdAt) {
        this(orderApproval, restaurantId, failureMessages, null, createdAt);
    }

    public OrderRejectedEvent(OrderApproval orderApproval,
                              RestaurantId restaurantId,
                              List<String> failureMessages,
                              CancelReason cancelReason,
                              ZonedDateTime createdAt) {
        super(orderApproval, restaurantId, failureMessages, createdAt);
        this.cancelReason = cancelReason;
    }

    public CancelReason getCancelReason() {
        return cancelReason;
    }
}
