package com.berkay.order.service.domain;

import com.berkay.domain.valueobject.OrderId;
import com.berkay.domain.valueobject.OrderStatus;
import com.berkay.order.service.domain.entity.Order;
import com.berkay.order.service.domain.exception.OrderNotFoundException;
import com.berkay.order.service.domain.outbox.scheduler.approval.ApprovalOutboxHelper;
import com.berkay.order.service.domain.outbox.scheduler.payment.PaymentOutboxHelper;
import com.berkay.order.service.domain.ports.output.repository.OrderRepository;
import com.berkay.saga.SagaStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Component
public class OrderSagaHelper {

    private final OrderRepository orderRepository;
    private final PaymentOutboxHelper paymentOutboxHelper;
    private final ApprovalOutboxHelper approvalOutboxHelper;

    public OrderSagaHelper(OrderRepository orderRepository,
                           PaymentOutboxHelper paymentOutboxHelper,
                           ApprovalOutboxHelper approvalOutboxHelper) {
        this.orderRepository = orderRepository;
        this.paymentOutboxHelper = paymentOutboxHelper;
        this.approvalOutboxHelper = approvalOutboxHelper;
    }

    public Order findOrder(String orderId) {
        Optional<Order> optionalOrder = orderRepository.findByIdWithLock(new OrderId(UUID.fromString(orderId)));
        if (optionalOrder.isEmpty()) {
            log.error("Order with id: {} could not be found!", orderId);
            throw new OrderNotFoundException("Order with id " + orderId + " could not be found!");
        }
        return optionalOrder.get();
    }

    void saveOrder(Order order) {
        orderRepository.save(order);
    }

    public SagaStatus orderStatusToSagaStatus(OrderStatus orderStatus) {
        return switch (orderStatus) {
            case PENDING -> SagaStatus.STARTED;
            case PAID, WAITING_FOR_APPROVAL, APPROVED, PREPARING, READY_FOR_PICKUP, IN_DELIVERY -> SagaStatus.PROCESSING;
            case DELIVERED -> SagaStatus.SUCCEEDED;
            case CANCELLING -> SagaStatus.COMPENSATING;
            case CANCELLED -> SagaStatus.COMPENSATED;
        };
    }

    @Transactional
    public void sealSagaAsSucceeded(UUID orderId) {
        paymentOutboxHelper.getPaymentOutboxMessageBySagaId(orderId).ifPresent(po -> {
            po.setSagaStatus(SagaStatus.SUCCEEDED);
            po.setOrderStatus(OrderStatus.DELIVERED);
            paymentOutboxHelper.save(po);
        });
        approvalOutboxHelper.getApprovalOutboxMessageBySagaId(orderId).ifPresent(ao -> {
            ao.setSagaStatus(SagaStatus.SUCCEEDED);
            ao.setOrderStatus(OrderStatus.DELIVERED);
            approvalOutboxHelper.save(ao);
        });
    }
}
