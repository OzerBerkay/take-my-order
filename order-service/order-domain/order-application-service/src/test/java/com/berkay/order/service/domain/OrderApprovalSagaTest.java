package com.berkay.order.service.domain;

import com.berkay.domain.valueobject.*;
import com.berkay.order.service.domain.dto.message.RestaurantApprovalResponse;
import com.berkay.order.service.domain.entity.Order;
import com.berkay.order.service.domain.mapper.OrderDataMapper;
import com.berkay.order.service.domain.outbox.model.approval.OrderApprovalOutboxMessage;
import com.berkay.order.service.domain.outbox.model.payment.OrderPaymentEventPayload;
import com.berkay.order.service.domain.outbox.model.payment.OrderPaymentOutboxMessage;
import com.berkay.order.service.domain.outbox.scheduler.approval.ApprovalOutboxHelper;
import com.berkay.order.service.domain.outbox.scheduler.payment.PaymentOutboxHelper;
import com.berkay.outbox.OutboxStatus;
import com.berkay.saga.SagaStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderApprovalSagaTest {

    @Mock
    private OrderDomainService orderDomainService;
    @Mock
    private OrderSagaHelper orderSagaHelper;
    @Mock
    private ApprovalOutboxHelper approvalOutboxHelper;
    @Mock
    private PaymentOutboxHelper paymentOutboxHelper;
    @Mock
    private OrderDataMapper orderDataMapper;
    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private OrderApprovalSaga orderApprovalSaga;

    private UUID orderId;
    private UUID sagaId;
    private Order order;
    private OrderApprovalOutboxMessage approvalOutboxMessage;
    private OrderPaymentOutboxMessage paymentOutboxMessage;

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
                .orderStatus(OrderStatus.PAID)
                .build();

        approvalOutboxMessage = OrderApprovalOutboxMessage.builder()
                .id(UUID.randomUUID())
                .sagaId(sagaId)
                .orderStatus(OrderStatus.PAID)
                .sagaStatus(SagaStatus.PROCESSING)
                .outboxStatus(OutboxStatus.STARTED)
                .version(1)
                .build();

        paymentOutboxMessage = OrderPaymentOutboxMessage.builder()
                .id(UUID.randomUUID())
                .sagaId(sagaId)
                .orderStatus(OrderStatus.PAID)
                .sagaStatus(SagaStatus.PROCESSING)
                .outboxStatus(OutboxStatus.COMPLETED)
                .version(3)
                .build();
    }

    @Test
    @DisplayName("Senaryo 1.2.A: Happy Path - VALIDATED response moves order to WAITING_FOR_APPROVAL and completes AO_1")
    void testProcessApprovalHappyPath() {
        RestaurantApprovalResponse response = RestaurantApprovalResponse.builder()
                .id(UUID.randomUUID().toString())
                .sagaId(sagaId.toString())
                .orderId(orderId.toString())
                .restaurantId(UUID.randomUUID().toString())
                .createdAt(Instant.now())
                .orderApprovalStatus(OrderApprovalStatus.VALIDATED)
                .build();

        when(orderSagaHelper.findOrder(orderId.toString())).thenReturn(order);
        when(approvalOutboxHelper.getApprovalOutboxMessageBySagaIdAndSagaStatus(sagaId, SagaStatus.PROCESSING))
                .thenReturn(Optional.of(approvalOutboxMessage));

        doAnswer(inv -> {
            order.waitForApproval();
            return null;
        }).when(orderDomainService).waitForApproval(order);

        orderApprovalSaga.process(response);

        assertEquals(OrderStatus.WAITING_FOR_APPROVAL, order.getOrderStatus());
        verify(orderDomainService).waitForApproval(order);
        verify(orderSagaHelper).saveOrder(order);
        verify(approvalOutboxHelper).save(argThat(msg ->
                msg.getOrderStatus() == OrderStatus.WAITING_FOR_APPROVAL
                        && msg.getSagaStatus() == SagaStatus.PROCESSING
                        && msg.getOutboxStatus() == OutboxStatus.COMPLETED));
        verifyNoInteractions(paymentOutboxHelper);
    }

    @Test
    @DisplayName("Senaryo 1.2.B: Auto-Reject - Restaurant rejects before stock deduction, seals AO_1 locally and re-arms PO_1 for refund")
    void testRollbackBranch1_AutoReject() {
        RestaurantApprovalResponse response = RestaurantApprovalResponse.builder()
                .id(UUID.randomUUID().toString())
                .sagaId(sagaId.toString())
                .orderId(orderId.toString())
                .restaurantId(UUID.randomUUID().toString())
                .createdAt(Instant.now())
                .orderApprovalStatus(OrderApprovalStatus.REJECTED)
                .cancelReason(CancelReason.AUTO_REJECTED_OUT_OF_STOCK.name())
                .build();

        when(approvalOutboxHelper.getApprovalOutboxMessageBySagaIdAndSagaStatus(sagaId, SagaStatus.COMPENSATING, SagaStatus.PROCESSING))
                .thenReturn(Optional.of(approvalOutboxMessage));
        when(orderSagaHelper.findOrder(orderId.toString())).thenReturn(order);
        when(paymentOutboxHelper.getPaymentOutboxMessageBySagaIdAndSagaStatus(sagaId, SagaStatus.PROCESSING))
                .thenReturn(Optional.of(paymentOutboxMessage));
        when(orderDataMapper.orderToOrderPaymentEventPayload(any(), eq(PaymentOrderStatus.CANCELLED)))
                .thenReturn(OrderPaymentEventPayload.builder().build());

        orderApprovalSaga.rollback(response);

        assertEquals(OrderStatus.CANCELLING, order.getOrderStatus());
        assertEquals(CancelReason.AUTO_REJECTED_OUT_OF_STOCK, order.getCancelReason());
        verify(orderSagaHelper).saveOrder(order);

        // AO_1 sealed locally as COMPENSATED + COMPLETED
        verify(approvalOutboxHelper).save(argThat(msg ->
                msg.getOrderStatus() == OrderStatus.CANCELLING
                        && msg.getSagaStatus() == SagaStatus.COMPENSATED
                        && msg.getOutboxStatus() == OutboxStatus.COMPLETED));

        // PO_1 re-armed as COMPENSATING + STARTED
        verify(paymentOutboxHelper).save(argThat(msg ->
                msg.getOrderStatus() == OrderStatus.CANCELLING
                        && msg.getSagaStatus() == SagaStatus.COMPENSATING
                        && msg.getOutboxStatus() == OutboxStatus.STARTED));
    }

    @Test
    @DisplayName("Senaryo 1.2.C: Chef Cancel - Stock restored after payment refunded completes Fork-Join barrier and cancels order")
    void testRollbackBranch2_ChefCancelStockRestored_ForkJoinBarrierComplete() {
        RestaurantApprovalResponse response = RestaurantApprovalResponse.builder()
                .id(UUID.randomUUID().toString())
                .sagaId(sagaId.toString())
                .orderId(orderId.toString())
                .restaurantId(UUID.randomUUID().toString())
                .createdAt(Instant.now())
                .orderApprovalStatus(OrderApprovalStatus.REJECTED)
                .cancelReason(null)
                .build();

        // Order is in CANCELLING state
        order.initCancel(CancelReason.OPERATIONAL_DISRUPTIONS, MaterialAction.RESTORE_STOCK, null);

        // AO_1 was COMPENSATING
        approvalOutboxMessage.setSagaStatus(SagaStatus.COMPENSATING);
        approvalOutboxMessage.setOrderStatus(OrderStatus.CANCELLING);

        // PO_1 was already COMPENSATED (refund arrived first)
        paymentOutboxMessage.setSagaStatus(SagaStatus.COMPENSATED);
        paymentOutboxMessage.setOrderStatus(OrderStatus.CANCELLING);

        when(approvalOutboxHelper.getApprovalOutboxMessageBySagaIdAndSagaStatus(sagaId, SagaStatus.COMPENSATING, SagaStatus.PROCESSING))
                .thenReturn(Optional.of(approvalOutboxMessage));
        when(paymentOutboxHelper.getPaymentOutboxMessageBySagaId(sagaId))
                .thenReturn(Optional.of(paymentOutboxMessage));
        when(orderSagaHelper.findOrder(orderId.toString())).thenReturn(order);

        orderApprovalSaga.rollback(response);

        assertEquals(OrderStatus.CANCELLED, order.getOrderStatus());
        verify(orderSagaHelper).saveOrder(order);

        // Both outboxes sealed as CANCELLED
        verify(approvalOutboxHelper, atLeastOnce()).save(argThat(msg ->
                msg.getOrderStatus() == OrderStatus.CANCELLED
                        && msg.getSagaStatus() == SagaStatus.COMPENSATED));
        verify(paymentOutboxHelper).save(argThat(msg ->
                msg.getOrderStatus() == OrderStatus.CANCELLED
                        && msg.getSagaStatus() == SagaStatus.COMPENSATED));
    }

    @Test
    @DisplayName("Senaryo 1.2.C: Chef Cancel - Stock restored while refund still pending leaves order in CANCELLING")
    void testRollbackBranch2_ChefCancelStockRestored_AwaitingPaymentRefund() {
        RestaurantApprovalResponse response = RestaurantApprovalResponse.builder()
                .id(UUID.randomUUID().toString())
                .sagaId(sagaId.toString())
                .orderId(orderId.toString())
                .restaurantId(UUID.randomUUID().toString())
                .createdAt(Instant.now())
                .orderApprovalStatus(OrderApprovalStatus.REJECTED)
                .cancelReason(null)
                .build();

        order.initCancel(CancelReason.OPERATIONAL_DISRUPTIONS, MaterialAction.RESTORE_STOCK, null);

        approvalOutboxMessage.setSagaStatus(SagaStatus.COMPENSATING);
        approvalOutboxMessage.setOrderStatus(OrderStatus.CANCELLING);

        // PO_1 still COMPENSATING (refund not completed yet)
        paymentOutboxMessage.setSagaStatus(SagaStatus.COMPENSATING);
        paymentOutboxMessage.setOrderStatus(OrderStatus.CANCELLING);

        when(approvalOutboxHelper.getApprovalOutboxMessageBySagaIdAndSagaStatus(sagaId, SagaStatus.COMPENSATING, SagaStatus.PROCESSING))
                .thenReturn(Optional.of(approvalOutboxMessage));
        when(paymentOutboxHelper.getPaymentOutboxMessageBySagaId(sagaId))
                .thenReturn(Optional.of(paymentOutboxMessage));

        orderApprovalSaga.rollback(response);

        assertEquals(OrderStatus.CANCELLING, order.getOrderStatus());
        verify(orderSagaHelper, never()).saveOrder(order);
        // Only AO_1 marked COMPENSATED, PO_1 not touched
        verify(approvalOutboxHelper).save(argThat(msg ->
                msg.getSagaStatus() == SagaStatus.COMPENSATED
                        && msg.getOutboxStatus() == OutboxStatus.COMPLETED));
        verify(paymentOutboxHelper, never()).save(any());
    }

    @Test
    @DisplayName("Senaryo 1.2.C: Chef Cancel - Payment outbox empty completes Fork-Join directly")
    void testRollbackBranch2_ChefCancelStockRestored_PaymentOutboxEmpty_CompletesOrderCancelled() {
        RestaurantApprovalResponse response = RestaurantApprovalResponse.builder()
                .id(UUID.randomUUID().toString())
                .sagaId(sagaId.toString())
                .orderId(orderId.toString())
                .restaurantId(UUID.randomUUID().toString())
                .createdAt(Instant.now())
                .orderApprovalStatus(OrderApprovalStatus.REJECTED)
                .cancelReason(null)
                .build();

        order.initCancel(CancelReason.OPERATIONAL_DISRUPTIONS, MaterialAction.RESTORE_STOCK, null);

        approvalOutboxMessage.setSagaStatus(SagaStatus.COMPENSATING);
        approvalOutboxMessage.setOrderStatus(OrderStatus.CANCELLING);

        when(approvalOutboxHelper.getApprovalOutboxMessageBySagaIdAndSagaStatus(sagaId, SagaStatus.COMPENSATING, SagaStatus.PROCESSING))
                .thenReturn(Optional.of(approvalOutboxMessage));
        when(paymentOutboxHelper.getPaymentOutboxMessageBySagaId(sagaId))
                .thenReturn(Optional.empty());
        when(orderSagaHelper.findOrder(orderId.toString())).thenReturn(order);

        orderApprovalSaga.rollback(response);

        assertEquals(OrderStatus.CANCELLED, order.getOrderStatus());
        verify(orderSagaHelper).saveOrder(order);
        verify(approvalOutboxHelper, atLeastOnce()).save(argThat(msg ->
                msg.getOrderStatus() == OrderStatus.CANCELLED
                        && msg.getSagaStatus() == SagaStatus.COMPENSATED));
    }
}
