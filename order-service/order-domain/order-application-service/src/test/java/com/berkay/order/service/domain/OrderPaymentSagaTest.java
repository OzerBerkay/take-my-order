package com.berkay.order.service.domain;

import com.berkay.domain.valueobject.*;
import com.berkay.order.service.domain.dto.message.PaymentResponse;
import com.berkay.order.service.domain.entity.Order;
import com.berkay.order.service.domain.event.OrderPaidEvent;
import com.berkay.order.service.domain.mapper.OrderDataMapper;
import com.berkay.order.service.domain.outbox.model.approval.OrderApprovalEventPayload;
import com.berkay.order.service.domain.outbox.model.payment.OrderPaymentOutboxMessage;
import com.berkay.order.service.domain.outbox.scheduler.approval.ApprovalOutboxHelper;
import com.berkay.order.service.domain.outbox.scheduler.payment.PaymentOutboxHelper;
import com.berkay.order.service.domain.ports.output.repository.OrderRepository;
import com.berkay.outbox.OutboxStatus;
import com.berkay.saga.SagaStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderPaymentSagaTest {

    @Mock
    private OrderDomainService orderDomainService;
    @Mock
    private OrderSagaHelper orderSagaHelper;
    @Mock
    private PaymentOutboxHelper paymentOutboxHelper;
    @Mock
    private ApprovalOutboxHelper approvalOutboxHelper;
    @Mock
    private OrderDataMapper orderDataMapper;
    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderPaymentSaga orderPaymentSaga;

    private UUID orderId;
    private UUID sagaId;
    private Order order;
    private OrderPaymentOutboxMessage outboxMessage;

    @BeforeEach
    void setUp() {
        orderId = UUID.randomUUID();
        sagaId = UUID.randomUUID();

        order = Order.builder()
                .orderId(new OrderId(orderId))
                .customerId(new CustomerId(UUID.randomUUID()))
                .restaurantId(new RestaurantId(UUID.randomUUID()))
                .price(new Money(new BigDecimal("100.00")))
                .items(Collections.emptyList())
                .orderStatus(OrderStatus.PENDING)
                .build();

        outboxMessage = OrderPaymentOutboxMessage.builder()
                .id(UUID.randomUUID())
                .sagaId(sagaId)
                .orderStatus(OrderStatus.PENDING)
                .sagaStatus(SagaStatus.STARTED)
                .outboxStatus(OutboxStatus.STARTED)
                .version(1)
                .build();
    }

    @Test
    @DisplayName("Senaryo 1.1.A: Happy Path - Successful payment completes order and arms approval outbox")
    void testProcessPaymentHappyPath() {
        PaymentResponse response = PaymentResponse.builder()
                .id(UUID.randomUUID().toString())
                .sagaId(sagaId.toString())
                .orderId(orderId.toString())
                .paymentId(UUID.randomUUID().toString())
                .customerId(UUID.randomUUID().toString())
                .price(new BigDecimal("100.00"))
                .createdAt(Instant.now())
                .paymentStatus(PaymentStatus.COMPLETED)
                .build();

        when(paymentOutboxHelper.getPaymentOutboxMessageBySagaIdAndSagaStatus(sagaId, SagaStatus.STARTED))
                .thenReturn(Optional.of(outboxMessage));
        when(orderSagaHelper.findOrder(orderId.toString())).thenReturn(order);

        OrderPaidEvent paidEvent = new OrderPaidEvent(order, ZonedDateTime.now(ZoneId.of("UTC")));
        when(orderDomainService.payOrder(order)).thenAnswer(inv -> {
            order.pay();
            return paidEvent;
        });
        when(orderSagaHelper.orderStatusToSagaStatus(OrderStatus.PAID)).thenReturn(SagaStatus.PROCESSING);
        when(orderDataMapper.orderPaidEventToOrderApprovalEventPayload(paidEvent))
                .thenReturn(OrderApprovalEventPayload.builder().build());

        orderPaymentSaga.process(response);

        verify(orderDomainService).payOrder(order);
        verify(orderRepository).save(order);
        verify(paymentOutboxHelper).save(any(OrderPaymentOutboxMessage.class));
        verify(approvalOutboxHelper).saveApprovalOutboxMessage(
                any(), eq(OrderStatus.PAID), eq(SagaStatus.PROCESSING), eq(OutboxStatus.STARTED), eq(sagaId));
    }

    @Test
    @DisplayName("Senaryo 1.1.B: Fail Path - Payment failed cancels order directly and marks PO_1 FAILED")
    void testRollbackPaymentFailed() {
        PaymentResponse response = PaymentResponse.builder()
                .id(UUID.randomUUID().toString())
                .sagaId(sagaId.toString())
                .orderId(orderId.toString())
                .paymentId(UUID.randomUUID().toString())
                .customerId(UUID.randomUUID().toString())
                .price(new BigDecimal("100.00"))
                .createdAt(Instant.now())
                .paymentStatus(PaymentStatus.FAILED)
                .build();

        when(paymentOutboxHelper.getPaymentOutboxMessageBySagaIdAndSagaStatus(eq(sagaId), any(SagaStatus[].class)))
                .thenReturn(Optional.of(outboxMessage));
        when(orderSagaHelper.findOrder(orderId.toString())).thenReturn(order);

        orderPaymentSaga.rollback(response);

        org.junit.jupiter.api.Assertions.assertEquals(OrderStatus.CANCELLED, order.getOrderStatus());
        org.junit.jupiter.api.Assertions.assertEquals(CancelReason.PAYMENT_FAILED, order.getCancelReason());
        verify(orderRepository).save(order);
        verify(paymentOutboxHelper).save(argThat(msg ->
                msg.getOrderStatus() == OrderStatus.CANCELLED && msg.getSagaStatus() == SagaStatus.FAILED));
        verify(approvalOutboxHelper).getApprovalOutboxMessageBySagaId(sagaId);
    }

    @Test
    @DisplayName("Senaryo 1.2.C: Rollback Payment Cancelled - Approval outbox empty bypasses barrier and finalizes order")
    void testRollbackPaymentCancelled_ApprovalOutboxEmpty_CompletesOrderCancelled() {
        order.pay();
        order.initCancel(CancelReason.OPERATIONAL_DISRUPTIONS, MaterialAction.RESTORE_STOCK, null);
        outboxMessage.setSagaStatus(SagaStatus.COMPENSATING);
        outboxMessage.setOrderStatus(OrderStatus.CANCELLING);

        PaymentResponse response = PaymentResponse.builder()
                .id(UUID.randomUUID().toString())
                .sagaId(sagaId.toString())
                .orderId(orderId.toString())
                .paymentId(UUID.randomUUID().toString())
                .customerId(UUID.randomUUID().toString())
                .price(new BigDecimal("100.00"))
                .createdAt(Instant.now())
                .paymentStatus(PaymentStatus.CANCELLED)
                .build();

        when(paymentOutboxHelper.getPaymentOutboxMessageBySagaIdAndSagaStatus(eq(sagaId), any(SagaStatus[].class)))
                .thenReturn(Optional.of(outboxMessage));
        when(orderSagaHelper.findOrder(orderId.toString())).thenReturn(order);
        when(approvalOutboxHelper.getApprovalOutboxMessageBySagaId(sagaId)).thenReturn(Optional.empty());

        orderPaymentSaga.rollback(response);

        org.junit.jupiter.api.Assertions.assertEquals(OrderStatus.CANCELLED, order.getOrderStatus());
        verify(orderRepository).save(order);
        verify(paymentOutboxHelper, atLeastOnce()).save(argThat(msg ->
                msg.getOrderStatus() == OrderStatus.CANCELLED && msg.getSagaStatus() == SagaStatus.COMPENSATED));
    }

    @Test
    @DisplayName("Senaryo 1.2.C: Rollback Payment Cancelled - Approval outbox COMPENSATED completes Fork-Join")
    void testRollbackPaymentCancelled_ApprovalOutboxCompensated_CompletesForkJoin() {
        order.pay();
        order.initCancel(CancelReason.OPERATIONAL_DISRUPTIONS, MaterialAction.RESTORE_STOCK, null);
        outboxMessage.setSagaStatus(SagaStatus.COMPENSATING);
        outboxMessage.setOrderStatus(OrderStatus.CANCELLING);

        com.berkay.order.service.domain.outbox.model.approval.OrderApprovalOutboxMessage approvalMsg =
                com.berkay.order.service.domain.outbox.model.approval.OrderApprovalOutboxMessage.builder()
                        .id(UUID.randomUUID())
                        .sagaId(sagaId)
                        .orderStatus(OrderStatus.CANCELLING)
                        .sagaStatus(SagaStatus.COMPENSATED)
                        .outboxStatus(OutboxStatus.COMPLETED)
                        .build();

        PaymentResponse response = PaymentResponse.builder()
                .id(UUID.randomUUID().toString())
                .sagaId(sagaId.toString())
                .orderId(orderId.toString())
                .paymentId(UUID.randomUUID().toString())
                .customerId(UUID.randomUUID().toString())
                .price(new BigDecimal("100.00"))
                .createdAt(Instant.now())
                .paymentStatus(PaymentStatus.CANCELLED)
                .build();

        when(paymentOutboxHelper.getPaymentOutboxMessageBySagaIdAndSagaStatus(eq(sagaId), any(SagaStatus[].class)))
                .thenReturn(Optional.of(outboxMessage));
        when(orderSagaHelper.findOrder(orderId.toString())).thenReturn(order);
        when(approvalOutboxHelper.getApprovalOutboxMessageBySagaId(sagaId)).thenReturn(Optional.of(approvalMsg));

        orderPaymentSaga.rollback(response);

        org.junit.jupiter.api.Assertions.assertEquals(OrderStatus.CANCELLED, order.getOrderStatus());
        verify(orderRepository).save(order);
        verify(paymentOutboxHelper, atLeastOnce()).save(argThat(msg ->
                msg.getOrderStatus() == OrderStatus.CANCELLED && msg.getSagaStatus() == SagaStatus.COMPENSATED));
        verify(approvalOutboxHelper).save(argThat(msg ->
                msg.getOrderStatus() == OrderStatus.CANCELLED && msg.getSagaStatus() == SagaStatus.COMPENSATED));
    }

    @Test
    @DisplayName("Senaryo 1.2.C: Rollback Payment Cancelled - Approval outbox still COMPENSATING leaves order CANCELLING")
    void testRollbackPaymentCancelled_ApprovalOutboxCompensating_LeavesCancelling() {
        order.pay();
        order.initCancel(CancelReason.OPERATIONAL_DISRUPTIONS, MaterialAction.RESTORE_STOCK, null);
        outboxMessage.setSagaStatus(SagaStatus.COMPENSATING);
        outboxMessage.setOrderStatus(OrderStatus.CANCELLING);

        com.berkay.order.service.domain.outbox.model.approval.OrderApprovalOutboxMessage approvalMsg =
                com.berkay.order.service.domain.outbox.model.approval.OrderApprovalOutboxMessage.builder()
                        .id(UUID.randomUUID())
                        .sagaId(sagaId)
                        .orderStatus(OrderStatus.CANCELLING)
                        .sagaStatus(SagaStatus.COMPENSATING)
                        .outboxStatus(OutboxStatus.STARTED)
                        .build();

        PaymentResponse response = PaymentResponse.builder()
                .id(UUID.randomUUID().toString())
                .sagaId(sagaId.toString())
                .orderId(orderId.toString())
                .paymentId(UUID.randomUUID().toString())
                .customerId(UUID.randomUUID().toString())
                .price(new BigDecimal("100.00"))
                .createdAt(Instant.now())
                .paymentStatus(PaymentStatus.CANCELLED)
                .build();

        when(paymentOutboxHelper.getPaymentOutboxMessageBySagaIdAndSagaStatus(eq(sagaId), any(SagaStatus[].class)))
                .thenReturn(Optional.of(outboxMessage));
        when(orderSagaHelper.findOrder(orderId.toString())).thenReturn(order);
        when(approvalOutboxHelper.getApprovalOutboxMessageBySagaId(sagaId)).thenReturn(Optional.of(approvalMsg));

        orderPaymentSaga.rollback(response);

        org.junit.jupiter.api.Assertions.assertEquals(OrderStatus.CANCELLING, order.getOrderStatus());
        verify(orderRepository, never()).save(order);
        verify(paymentOutboxHelper).save(argThat(msg ->
                msg.getSagaStatus() == SagaStatus.COMPENSATED && msg.getOutboxStatus() == OutboxStatus.COMPLETED));
    }

    @Test
    @DisplayName("Fail Path: Payment failed during cancellation rollback preserves original cancel reason")
    void testRollbackPaymentFailed_DuringCancelRollback_PreservesCancelReason() {
        order.pay();
        order.initCancel(CancelReason.OPERATIONAL_DISRUPTIONS, MaterialAction.RESTORE_STOCK, null);
        outboxMessage.setSagaStatus(SagaStatus.COMPENSATING);
        outboxMessage.setOrderStatus(OrderStatus.CANCELLING);

        PaymentResponse response = PaymentResponse.builder()
                .id(UUID.randomUUID().toString())
                .sagaId(sagaId.toString())
                .orderId(orderId.toString())
                .paymentId(UUID.randomUUID().toString())
                .customerId(UUID.randomUUID().toString())
                .price(new BigDecimal("100.00"))
                .createdAt(Instant.now())
                .paymentStatus(PaymentStatus.FAILED)
                .build();

        when(paymentOutboxHelper.getPaymentOutboxMessageBySagaIdAndSagaStatus(eq(sagaId), any(SagaStatus[].class)))
                .thenReturn(Optional.of(outboxMessage));
        when(orderSagaHelper.findOrder(orderId.toString())).thenReturn(order);

        orderPaymentSaga.rollback(response);

        org.junit.jupiter.api.Assertions.assertEquals(OrderStatus.CANCELLED, order.getOrderStatus());
        org.junit.jupiter.api.Assertions.assertEquals(CancelReason.OPERATIONAL_DISRUPTIONS, order.getCancelReason());
        verify(orderRepository).save(order);
        verify(paymentOutboxHelper).save(argThat(msg ->
                msg.getOrderStatus() == OrderStatus.CANCELLED && msg.getSagaStatus() == SagaStatus.FAILED));
    }
}
