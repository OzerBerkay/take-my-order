package com.berkay.order.service.domain;

import com.berkay.domain.valueobject.CancelReason;
import com.berkay.domain.valueobject.MaterialAction;
import com.berkay.domain.valueobject.OrderStatus;
import com.berkay.domain.valueobject.PaymentOrderStatus;
import com.berkay.domain.valueobject.RestaurantOrderStatus;
import com.berkay.order.service.domain.dto.operation.CancelOrderCommand;
import com.berkay.order.service.domain.entity.Order;
import com.berkay.order.service.domain.exception.OrderConflictException;
import com.berkay.order.service.domain.exception.OrderDomainException;
import com.berkay.order.service.domain.mapper.OrderDataMapper;
import com.berkay.order.service.domain.outbox.model.approval.OrderApprovalEventPayload;
import com.berkay.order.service.domain.outbox.model.payment.OrderPaymentEventPayload;
import com.berkay.order.service.domain.outbox.scheduler.approval.ApprovalOutboxHelper;
import com.berkay.order.service.domain.outbox.scheduler.payment.PaymentOutboxHelper;
import com.berkay.order.service.domain.ports.output.repository.OrderRepository;
import com.berkay.outbox.OutboxStatus;
import com.berkay.saga.SagaStatus;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
public class OrderCancelHelper {

    private final OrderRepository orderRepository;
    private final OrderDomainService orderDomainService;
    private final PaymentOutboxHelper paymentOutboxHelper;
    private final ApprovalOutboxHelper approvalOutboxHelper;
    private final OrderDataMapper orderDataMapper;
    private final ObjectMapper objectMapper;

    public OrderCancelHelper(OrderRepository orderRepository,
                             OrderDomainService orderDomainService,
                             PaymentOutboxHelper paymentOutboxHelper,
                             ApprovalOutboxHelper approvalOutboxHelper,
                             OrderDataMapper orderDataMapper,
                             ObjectMapper objectMapper) {
        this.orderRepository = orderRepository;
        this.orderDomainService = orderDomainService;
        this.paymentOutboxHelper = paymentOutboxHelper;
        this.approvalOutboxHelper = approvalOutboxHelper;
        this.orderDataMapper = orderDataMapper;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public void initCancelOrder(Order order, CancelOrderCommand command) {
        MaterialAction materialAction = validateAndDetermineMaterialAction(
                order.getOrderStatus(), command.getCancelReason(), command.getMaterialAction());

        boolean isCustomerFault = (order.getOrderStatus() == OrderStatus.IN_DELIVERY) &&
                (command.getCancelReason() == CancelReason.CUSTOMER_UNREACHABLE ||
                        command.getCancelReason() == CancelReason.WRONG_ADDRESS);

        if (isCustomerFault) {
            orderDomainService.cancelOrderWithoutRefund(order, command.getCancelReason(), MaterialAction.RECORD_WASTE, command.getInternalNote());
            orderRepository.save(order);

            paymentOutboxHelper.getPaymentOutboxMessageBySagaId(order.getId().getValue()).ifPresent(po -> {
                po.setOrderStatus(OrderStatus.CANCELLED);
                po.setSagaStatus(SagaStatus.COMPENSATED);
                po.setOutboxStatus(OutboxStatus.COMPLETED);
                paymentOutboxHelper.save(po);
            });

            approvalOutboxHelper.getApprovalOutboxMessageBySagaId(order.getId().getValue()).ifPresent(ao -> {
                ao.setOrderStatus(OrderStatus.CANCELLED);
                ao.setSagaStatus(SagaStatus.COMPENSATED);
                ao.setOutboxStatus(OutboxStatus.COMPLETED);
                approvalOutboxHelper.save(ao);
            });

            log.info("Order with id: {} cancelled without refund due to customer fault (0 Kafka messages).",
                    order.getId().getValue());
        } else {
            order.initCancel(command.getCancelReason(), materialAction, command.getInternalNote());
            orderRepository.save(order);

            paymentOutboxHelper.getPaymentOutboxMessageBySagaId(order.getId().getValue()).ifPresent(po -> {
                po.setOrderStatus(OrderStatus.CANCELLING);
                po.setSagaStatus(SagaStatus.COMPENSATING);
                po.setOutboxStatus(OutboxStatus.STARTED);
                po.setPayload(createPaymentPayload(
                        orderDataMapper.orderToOrderPaymentEventPayload(order, PaymentOrderStatus.CANCELLED)));
                paymentOutboxHelper.save(po);
            });

            approvalOutboxHelper.getApprovalOutboxMessageBySagaId(order.getId().getValue()).ifPresent(ao -> {
                if (materialAction == MaterialAction.RESTORE_STOCK) {
                    ao.setOrderStatus(OrderStatus.CANCELLING);
                    ao.setSagaStatus(SagaStatus.COMPENSATING);
                    ao.setOutboxStatus(OutboxStatus.STARTED);
                    ao.setPayload(createApprovalPayload(
                            orderDataMapper.orderToOrderApprovalEventPayload(order, RestaurantOrderStatus.CANCELLED)));
                    approvalOutboxHelper.save(ao);
                } else {
                    ao.setOrderStatus(OrderStatus.CANCELLING);
                    ao.setSagaStatus(SagaStatus.COMPENSATED);
                    ao.setOutboxStatus(OutboxStatus.COMPLETED);
                    approvalOutboxHelper.save(ao);
                }
            });

            log.info("Order with id: {} initCancel executed with reason: {} and materialAction: {}.",
                    order.getId().getValue(), command.getCancelReason(), materialAction);
        }
    }

    public MaterialAction validateAndDetermineMaterialAction(OrderStatus currentStatus,
                                                             CancelReason cancelReason,
                                                             MaterialAction clientMaterialAction) {
        return switch (currentStatus) {
            case WAITING_FOR_APPROVAL -> {
                if (cancelReason != CancelReason.OPERATIONAL_DISRUPTIONS && cancelReason != CancelReason.INSUFFICIENT_STOCK) {
                    throw new OrderDomainException("In WAITING_FOR_APPROVAL state, cancelReason must be OPERATIONAL_DISRUPTIONS or INSUFFICIENT_STOCK!");
                }
                yield cancelReason == CancelReason.OPERATIONAL_DISRUPTIONS
                        ? MaterialAction.RESTORE_STOCK
                        : MaterialAction.RECORD_WASTE;
            }
            case APPROVED -> {
                if (cancelReason != CancelReason.OPERATIONAL_DISRUPTIONS && cancelReason != CancelReason.INSUFFICIENT_STOCK) {
                    throw new OrderDomainException("In APPROVED state, cancelReason must be OPERATIONAL_DISRUPTIONS or INSUFFICIENT_STOCK!");
                }
                if (cancelReason == CancelReason.INSUFFICIENT_STOCK) {
                    if (clientMaterialAction == MaterialAction.RESTORE_STOCK) {
                        throw new OrderDomainException("In APPROVED state with INSUFFICIENT_STOCK, food cannot be restored to stock!");
                    }
                    yield MaterialAction.RECORD_WASTE;
                } else { // OPERATIONAL_DISRUPTIONS
                    if (clientMaterialAction == null) {
                        throw new OrderDomainException("In APPROVED state with OPERATIONAL_DISRUPTIONS, materialAction (RESTORE_STOCK or RECORD_WASTE) is required!");
                    }
                    yield clientMaterialAction;
                }
            }
            case PREPARING -> {
                if (cancelReason != CancelReason.OPERATIONAL_DISRUPTIONS && cancelReason != CancelReason.INSUFFICIENT_STOCK) {
                    throw new OrderDomainException("In PREPARING state, cancelReason must be OPERATIONAL_DISRUPTIONS or INSUFFICIENT_STOCK!");
                }
                if (cancelReason == CancelReason.INSUFFICIENT_STOCK) {
                    if (clientMaterialAction == MaterialAction.RESTORE_STOCK) {
                        throw new OrderDomainException("Food cannot be restored to stock in this scenario!");
                    }
                    yield MaterialAction.RECORD_WASTE;
                } else { // OPERATIONAL_DISRUPTIONS
                    if (clientMaterialAction == null) {
                        throw new OrderDomainException("In PREPARING state with OPERATIONAL_DISRUPTIONS, materialAction (RESTORE_STOCK or RECORD_WASTE) is required!");
                    }
                    yield clientMaterialAction;
                }
            }
            case READY_FOR_PICKUP -> {
                if (cancelReason != CancelReason.OPERATIONAL_DISRUPTIONS) {
                    throw new OrderDomainException("In READY_FOR_PICKUP state, cancelReason must be OPERATIONAL_DISRUPTIONS!");
                }
                if (clientMaterialAction == MaterialAction.RESTORE_STOCK) {
                    throw new OrderDomainException("In READY_FOR_PICKUP state, cooked food cannot be restored to stock!");
                }
                yield MaterialAction.RECORD_WASTE;
            }
            case IN_DELIVERY -> {
                if (cancelReason != CancelReason.LOGISTIC_DISRUPTIONS &&
                        cancelReason != CancelReason.CUSTOMER_UNREACHABLE &&
                        cancelReason != CancelReason.WRONG_ADDRESS) {
                    throw new OrderDomainException("In IN_DELIVERY state, kitchen reasons cannot be selected!");
                }
                if (clientMaterialAction == MaterialAction.RESTORE_STOCK) {
                    throw new OrderDomainException("In IN_DELIVERY state, food cannot be restored to stock!");
                }
                yield MaterialAction.RECORD_WASTE;
            }
            default -> throw new OrderConflictException("Order is in " + currentStatus + " state and cannot be cancelled via this endpoint!");
        };
    }

    public String createPaymentPayload(OrderPaymentEventPayload payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            log.error("Could not create OrderPaymentEventPayload JSON for order id: {}", payload.getOrderId(), e);
            throw new OrderDomainException("Could not create OrderPaymentEventPayload JSON for order id: " + payload.getOrderId(), e);
        }
    }

    public String createApprovalPayload(OrderApprovalEventPayload payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            log.error("Could not create OrderApprovalEventPayload JSON for order id: {}", payload.getOrderId(), e);
            throw new OrderDomainException("Could not create OrderApprovalEventPayload JSON for order id: " + payload.getOrderId(), e);
        }
    }
}
