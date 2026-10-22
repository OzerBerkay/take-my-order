package com.berkay.order.service.domain;

import com.berkay.domain.valueobject.*;
import com.berkay.order.service.domain.dto.operation.ForceCancelOrderCommand;
import com.berkay.order.service.domain.dto.restaurant.RestaurantOrderResponse;
import com.berkay.order.service.domain.entity.Order;
import com.berkay.order.service.domain.exception.OrderConflictException;
import com.berkay.order.service.domain.exception.OrderDomainException;
import com.berkay.order.service.domain.exception.OrderNotFoundException;
import com.berkay.order.service.domain.mapper.OrderDataMapper;
import com.berkay.order.service.domain.outbox.model.approval.OrderApprovalOutboxMessage;
import com.berkay.order.service.domain.outbox.model.payment.OrderPaymentOutboxMessage;
import com.berkay.order.service.domain.outbox.scheduler.approval.ApprovalOutboxHelper;
import com.berkay.order.service.domain.outbox.scheduler.payment.PaymentOutboxHelper;
import com.berkay.order.service.domain.ports.output.repository.OrderRepository;
import com.berkay.outbox.OutboxStatus;
import com.berkay.saga.SagaStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
public class OrderForceCancelCommandHandler {

    private final OrderRepository orderRepository;
    private final PaymentOutboxHelper paymentOutboxHelper;
    private final ApprovalOutboxHelper approvalOutboxHelper;
    private final OrderCancelHelper orderCancelHelper;
    private final OrderDataMapper orderDataMapper;

    public OrderForceCancelCommandHandler(OrderRepository orderRepository,
                                         PaymentOutboxHelper paymentOutboxHelper,
                                         ApprovalOutboxHelper approvalOutboxHelper,
                                         OrderCancelHelper orderCancelHelper,
                                         OrderDataMapper orderDataMapper) {
        this.orderRepository = orderRepository;
        this.paymentOutboxHelper = paymentOutboxHelper;
        this.approvalOutboxHelper = approvalOutboxHelper;
        this.orderCancelHelper = orderCancelHelper;
        this.orderDataMapper = orderDataMapper;
    }

    @Transactional
    public RestaurantOrderResponse forceCancelOrder(ForceCancelOrderCommand command) {
        Order order = orderRepository.findByIdWithLock(new OrderId(command.getOrderId()))
                .orElseThrow(() -> new OrderNotFoundException("Order with id " + command.getOrderId() + " could not be found!"));

        OrderStatus status = order.getOrderStatus();
        if (status == OrderStatus.PENDING || status == OrderStatus.PAID ||
                status == OrderStatus.CANCELLING || status == OrderStatus.DELIVERED ||
                status == OrderStatus.CANCELLED) {
            log.warn("Force cancel rejected. Order with id: {} is in protected state: {}",
                    order.getId().getValue(), status);
            throw new OrderConflictException("Order is in " + status + " state and cannot be force-cancelled!");
        }

        MaterialAction calculatedMaterialAction = (status == OrderStatus.READY_FOR_PICKUP || status == OrderStatus.IN_DELIVERY)
                ? MaterialAction.RECORD_WASTE
                : MaterialAction.RESTORE_STOCK;

        order.initCancel(CancelReason.OPERATIONAL_DISRUPTIONS, calculatedMaterialAction,
                "[ADMIN FORCE CANCEL] " + command.getAdminReason());
        orderRepository.save(order);

        OrderPaymentOutboxMessage po = paymentOutboxHelper.getPaymentOutboxMessageBySagaId(order.getId().getValue())
                .orElseThrow(() -> new OrderDomainException("Payment outbox message not found for order id: " + order.getId().getValue()));

        if (command.isForceRefund()) {
            po.setOrderStatus(OrderStatus.CANCELLING);
            po.setSagaStatus(SagaStatus.COMPENSATING);
            po.setOutboxStatus(OutboxStatus.STARTED);
            po.setPayload(orderCancelHelper.createPaymentPayload(
                    orderDataMapper.orderToOrderPaymentEventPayload(order, PaymentOrderStatus.CANCELLED)));
            paymentOutboxHelper.save(po);
        } else {
            po.setOrderStatus(OrderStatus.CANCELLING);
            po.setSagaStatus(SagaStatus.COMPENSATED);
            po.setOutboxStatus(OutboxStatus.COMPLETED);
            paymentOutboxHelper.save(po);
        }

        OrderApprovalOutboxMessage ao = approvalOutboxHelper.getApprovalOutboxMessageBySagaId(order.getId().getValue())
                .orElseThrow(() -> new OrderDomainException("Approval outbox message not found for order id: " + order.getId().getValue()));

        if (calculatedMaterialAction == MaterialAction.RESTORE_STOCK) {
            ao.setOrderStatus(OrderStatus.CANCELLING);
            ao.setSagaStatus(SagaStatus.COMPENSATING);
            ao.setOutboxStatus(OutboxStatus.STARTED);
            ao.setPayload(orderCancelHelper.createApprovalPayload(
                    orderDataMapper.orderToOrderApprovalEventPayload(order, RestaurantOrderStatus.CANCELLED)));
            approvalOutboxHelper.save(ao);
        } else {
            ao.setOrderStatus(OrderStatus.CANCELLING);
            ao.setSagaStatus(SagaStatus.COMPENSATED);
            ao.setOutboxStatus(OutboxStatus.COMPLETED);
            approvalOutboxHelper.save(ao);
        }

        if (!command.isForceRefund() && calculatedMaterialAction == MaterialAction.RECORD_WASTE) {
            order.cancel(order.getCancelReason());
            orderRepository.save(order);
            po.setOrderStatus(OrderStatus.CANCELLED);
            paymentOutboxHelper.save(po);
            ao.setOrderStatus(OrderStatus.CANCELLED);
            approvalOutboxHelper.save(ao);
            log.info("Order with id: {} is immediately finalized as CANCELLED (zero Kafka compensation required).",
                    order.getId().getValue());
        }

        log.info("Force cancel executed for order id: {} by admin. forceRefund: {}, materialAction: {}",
                order.getId().getValue(), command.isForceRefund(), calculatedMaterialAction);

        return orderDataMapper.orderToRestaurantOrderResponse(order);
    }
}
