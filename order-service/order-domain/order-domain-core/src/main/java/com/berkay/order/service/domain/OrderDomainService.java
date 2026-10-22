package com.berkay.order.service.domain;

import com.berkay.domain.valueobject.CancelReason;
import com.berkay.domain.valueobject.MaterialAction;
import com.berkay.order.service.domain.entity.Order;
import com.berkay.order.service.domain.entity.Restaurant;
import com.berkay.order.service.domain.event.OrderCancelledEvent;
import com.berkay.order.service.domain.event.OrderCreatedEvent;
import com.berkay.order.service.domain.event.OrderPaidEvent;

import java.util.List;

public interface OrderDomainService {

    OrderCreatedEvent validateAndInitiateOrder(Order order, Restaurant restaurant);

    OrderPaidEvent payOrder(Order order);

    void waitForApproval(Order order);

    void approveOrder(Order order);

    void startPreparing(Order order);

    void readyForPickup(Order order);

    void startDelivery(Order order);

    void deliver(Order order);

    OrderCancelledEvent cancelOrderPayment(Order order, List<String> failureMessages);

    OrderCancelledEvent cancelOrderPayment(Order order, CancelReason cancelReason, MaterialAction materialAction, String internalNote);

    void cancelOrder(Order order, List<String> failureMessages);

    void cancelOrder(Order order, CancelReason cancelReason);

    void cancelOrderWithoutRefund(Order order, CancelReason cancelReason, MaterialAction materialAction, String internalNote);
}
