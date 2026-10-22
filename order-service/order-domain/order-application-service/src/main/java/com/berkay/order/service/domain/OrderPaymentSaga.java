package com.berkay.order.service.domain;

import com.berkay.domain.valueobject.CancelReason;
import com.berkay.domain.valueobject.OrderStatus;
import com.berkay.domain.valueobject.PaymentStatus;
import com.berkay.order.service.domain.dto.message.PaymentResponse;
import com.berkay.order.service.domain.entity.Order;
import com.berkay.order.service.domain.event.OrderPaidEvent;
import com.berkay.order.service.domain.exception.OrderDomainException;
import com.berkay.order.service.domain.mapper.OrderDataMapper;
import com.berkay.order.service.domain.outbox.model.approval.OrderApprovalOutboxMessage;
import com.berkay.order.service.domain.outbox.model.payment.OrderPaymentOutboxMessage;
import com.berkay.order.service.domain.outbox.scheduler.approval.ApprovalOutboxHelper;
import com.berkay.order.service.domain.outbox.scheduler.payment.PaymentOutboxHelper;
import com.berkay.order.service.domain.ports.output.repository.OrderRepository;
import com.berkay.outbox.OutboxStatus;
import com.berkay.saga.SagaStatus;
import com.berkay.saga.SagaStep;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

import static com.berkay.domain.DomainConstants.UTC;

@Slf4j
@Component
public class OrderPaymentSaga implements SagaStep<PaymentResponse> {

    private final OrderDomainService orderDomainService;
    private final OrderSagaHelper orderSagaHelper;
    private final PaymentOutboxHelper paymentOutboxHelper;
    private final ApprovalOutboxHelper approvalOutboxHelper;
    private final OrderDataMapper orderDataMapper;
    private final OrderRepository orderRepository;

    public OrderPaymentSaga(OrderDomainService orderDomainService,
                            OrderSagaHelper orderSagaHelper,
                            PaymentOutboxHelper paymentOutboxHelper,
                            ApprovalOutboxHelper approvalOutboxHelper,
                            OrderDataMapper orderDataMapper,
                            OrderRepository orderRepository) {
        this.orderDomainService = orderDomainService;
        this.orderSagaHelper = orderSagaHelper;
        this.paymentOutboxHelper = paymentOutboxHelper;
        this.approvalOutboxHelper = approvalOutboxHelper;
        this.orderDataMapper = orderDataMapper;
        this.orderRepository = orderRepository;
    }

    @Override
    @Transactional
    public void process(PaymentResponse paymentResponse) {
        Optional<OrderPaymentOutboxMessage> orderPaymentOutboxMessageResponse =
                paymentOutboxHelper.getPaymentOutboxMessageBySagaIdAndSagaStatus(
                        UUID.fromString(paymentResponse.getSagaId()),
                        SagaStatus.STARTED);

        if (orderPaymentOutboxMessageResponse.isEmpty()) {
            log.info("An outbox message with saga id: {} is already processed!", paymentResponse.getSagaId());
            return;
        }

        Order order = orderSagaHelper.findOrder(paymentResponse.getOrderId());
        if (order.getOrderStatus() != OrderStatus.PENDING) {
            log.info("Order with id: {} is already processed, current status: {}", paymentResponse.getOrderId(), order.getOrderStatus());
            return;
        }

        OrderPaidEvent domainEvent = orderDomainService.payOrder(order);
        orderRepository.save(order);

        OrderPaymentOutboxMessage orderPaymentOutboxMessage = orderPaymentOutboxMessageResponse.get();
        SagaStatus sagaStatus = orderSagaHelper.orderStatusToSagaStatus(domainEvent.getOrder().getOrderStatus());

        paymentOutboxHelper.save(getUpdatedPaymentOutboxMessage(orderPaymentOutboxMessage,
                domainEvent.getOrder().getOrderStatus(), sagaStatus));

        approvalOutboxHelper.saveApprovalOutboxMessage(
                orderDataMapper.orderPaidEventToOrderApprovalEventPayload(domainEvent),
                domainEvent.getOrder().getOrderStatus(),
                sagaStatus,
                OutboxStatus.STARTED,
                UUID.fromString(paymentResponse.getSagaId()));

        log.info("Order with id: {} is paid", domainEvent.getOrder().getId().getValue());
    }

    @Override
    @Transactional
    public void rollback(PaymentResponse paymentResponse) {
        Optional<OrderPaymentOutboxMessage> orderPaymentOutboxMessageResponse =
                paymentOutboxHelper.getPaymentOutboxMessageBySagaIdAndSagaStatus(
                        UUID.fromString(paymentResponse.getSagaId()),
                        getCurrentSagaStatus(paymentResponse.getPaymentStatus()));

        if (orderPaymentOutboxMessageResponse.isEmpty()) {
            log.info("An outbox message with saga id: {} is already roll backed!", paymentResponse.getSagaId());
            return;
        }

        Order order = orderSagaHelper.findOrder(paymentResponse.getOrderId());
        if (order.getOrderStatus() == OrderStatus.CANCELLED) {
            log.info("Order with id: {} is already cancelled!", paymentResponse.getOrderId());
            return;
        }

        OrderPaymentOutboxMessage orderPaymentOutboxMessage = orderPaymentOutboxMessageResponse.get();

        if (paymentResponse.getPaymentStatus() == PaymentStatus.FAILED) {
            order.cancel(order.getCancelReason() != null ? order.getCancelReason() : CancelReason.PAYMENT_FAILED);
            orderRepository.save(order);

            orderPaymentOutboxMessage.setOrderStatus(OrderStatus.CANCELLED);
            orderPaymentOutboxMessage.setSagaStatus(SagaStatus.FAILED);
            orderPaymentOutboxMessage.setOutboxStatus(OutboxStatus.COMPLETED);
            orderPaymentOutboxMessage.setProcessedAt(ZonedDateTime.now(ZoneId.of(UTC)));
            paymentOutboxHelper.save(orderPaymentOutboxMessage);

            Optional<OrderApprovalOutboxMessage> approvalOutboxOpt = approvalOutboxHelper
                    .getApprovalOutboxMessageBySagaId(UUID.fromString(paymentResponse.getSagaId()));
            if (approvalOutboxOpt.isPresent()) {
                OrderApprovalOutboxMessage approvalOutboxMessage = approvalOutboxOpt.get();
                approvalOutboxMessage.setOrderStatus(OrderStatus.CANCELLED);
                approvalOutboxHelper.save(approvalOutboxMessage);
            }
            log.info("Order with id: {} is cancelled due to payment failure", order.getId().getValue());
        } else if (paymentResponse.getPaymentStatus() == PaymentStatus.CANCELLED) {
            orderPaymentOutboxMessage.setSagaStatus(SagaStatus.COMPENSATED);
            orderPaymentOutboxMessage.setOutboxStatus(OutboxStatus.COMPLETED);
            orderPaymentOutboxMessage.setProcessedAt(ZonedDateTime.now(ZoneId.of(UTC)));
            paymentOutboxHelper.save(orderPaymentOutboxMessage);

            // Fork-Join Kontrolü: Restoran tarafı da COMPENSATED mı (veya restoran outbox hiç yok mu)?
            Optional<OrderApprovalOutboxMessage> approvalOutboxOpt = approvalOutboxHelper
                    .getApprovalOutboxMessageBySagaId(UUID.fromString(paymentResponse.getSagaId()));

            boolean isApprovalCompensatedOrNotNeeded = approvalOutboxOpt.isEmpty()
                    || approvalOutboxOpt.get().getSagaStatus() == SagaStatus.COMPENSATED;

            if (isApprovalCompensatedOrNotNeeded) {
                order.cancel(order.getCancelReason());
                orderRepository.save(order);

                orderPaymentOutboxMessage.setOrderStatus(OrderStatus.CANCELLED);
                paymentOutboxHelper.save(orderPaymentOutboxMessage);

                if (approvalOutboxOpt.isPresent()) {
                    OrderApprovalOutboxMessage approvalOutboxMessage = approvalOutboxOpt.get();
                    approvalOutboxMessage.setOrderStatus(OrderStatus.CANCELLED);
                    approvalOutboxHelper.save(approvalOutboxMessage);
                }
                log.info("Order with id: {} is finalized as CANCELLED (Fork-Join barrier complete).", order.getId().getValue());
            } else {
                log.info("Order with id: {} remains in CANCELLING state, awaiting restaurant compensation.",
                        paymentResponse.getOrderId());
            }
        }
    }

    private OrderPaymentOutboxMessage getUpdatedPaymentOutboxMessage(OrderPaymentOutboxMessage orderPaymentOutboxMessage,
                                                                     OrderStatus orderStatus, SagaStatus sagaStatus) {
        orderPaymentOutboxMessage.setProcessedAt(ZonedDateTime.now(ZoneId.of(UTC)));
        orderPaymentOutboxMessage.setOrderStatus(orderStatus);
        orderPaymentOutboxMessage.setSagaStatus(sagaStatus);
        return orderPaymentOutboxMessage;
    }

    private SagaStatus[] getCurrentSagaStatus(PaymentStatus paymentStatus) {
        return switch (paymentStatus) {
            case COMPLETED -> new SagaStatus[] { SagaStatus.STARTED };
            case CANCELLED -> new SagaStatus[] { SagaStatus.COMPENSATING, SagaStatus.PROCESSING };
            case FAILED -> new SagaStatus[] { SagaStatus.STARTED, SagaStatus.PROCESSING, SagaStatus.COMPENSATING };
        };
    }
}
