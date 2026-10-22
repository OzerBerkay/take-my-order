package com.berkay.order.service.domain;

import com.berkay.domain.valueobject.*;
import com.berkay.order.service.domain.dto.operation.CancelOrderCommand;
import com.berkay.order.service.domain.entity.Order;
import com.berkay.order.service.domain.exception.OrderDomainException;
import com.berkay.order.service.domain.mapper.OrderDataMapper;
import com.berkay.order.service.domain.outbox.model.approval.OrderApprovalOutboxMessage;
import com.berkay.order.service.domain.outbox.model.payment.OrderPaymentOutboxMessage;
import com.berkay.order.service.domain.outbox.scheduler.approval.ApprovalOutboxHelper;
import com.berkay.order.service.domain.outbox.scheduler.payment.PaymentOutboxHelper;
import com.berkay.order.service.domain.ports.output.repository.OrderRepository;
import com.berkay.outbox.OutboxStatus;
import com.berkay.saga.SagaStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderCancelHelperTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private OrderDomainService orderDomainService;
    @Mock
    private PaymentOutboxHelper paymentOutboxHelper;
    @Mock
    private ApprovalOutboxHelper approvalOutboxHelper;
    @Mock
    private OrderDataMapper orderDataMapper;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private OrderCancelHelper helper;

    @BeforeEach
    void setUp() {
        helper = new OrderCancelHelper(orderRepository, orderDomainService, paymentOutboxHelper,
                approvalOutboxHelper, orderDataMapper, objectMapper);
    }

    @Test
    @DisplayName("Matrix WAITING_FOR_APPROVAL: OPERATIONAL_DISRUPTIONS -> RESTORE_STOCK and both outboxes re-armed")
    void testWaitingForApprovalOperationalDisruptions() {
        UUID orderId = UUID.randomUUID();
        CancelOrderCommand command = CancelOrderCommand.builder()
                .orderId(orderId)
                .currentStatus(OrderStatus.WAITING_FOR_APPROVAL)
                .cancelReason(CancelReason.OPERATIONAL_DISRUPTIONS)
                .build();

        Order order = mock(Order.class);
        when(order.getId()).thenReturn(new OrderId(orderId));
        when(order.getOrderStatus()).thenReturn(OrderStatus.WAITING_FOR_APPROVAL);

        OrderPaymentOutboxMessage po = OrderPaymentOutboxMessage.builder().sagaStatus(SagaStatus.PROCESSING).build();
        OrderApprovalOutboxMessage ao = OrderApprovalOutboxMessage.builder().sagaStatus(SagaStatus.PROCESSING).build();

        when(paymentOutboxHelper.getPaymentOutboxMessageBySagaId(orderId)).thenReturn(Optional.of(po));
        when(approvalOutboxHelper.getApprovalOutboxMessageBySagaId(orderId)).thenReturn(Optional.of(ao));

        helper.initCancelOrder(order, command);

        verify(order).initCancel(CancelReason.OPERATIONAL_DISRUPTIONS, MaterialAction.RESTORE_STOCK, null);
        verify(orderRepository).save(order);

        assertEquals(OrderStatus.CANCELLING, po.getOrderStatus());
        assertEquals(SagaStatus.COMPENSATING, po.getSagaStatus());
        assertEquals(OutboxStatus.STARTED, po.getOutboxStatus());

        assertEquals(OrderStatus.CANCELLING, ao.getOrderStatus());
        assertEquals(SagaStatus.COMPENSATING, ao.getSagaStatus());
        assertEquals(OutboxStatus.STARTED, ao.getOutboxStatus());
    }

    @Test
    @DisplayName("Matrix WAITING_FOR_APPROVAL: INSUFFICIENT_STOCK -> RECORD_WASTE and AO_1 locally sealed (0 kafka)")
    void testWaitingForApprovalInsufficientStock() {
        UUID orderId = UUID.randomUUID();
        CancelOrderCommand command = CancelOrderCommand.builder()
                .orderId(orderId)
                .currentStatus(OrderStatus.WAITING_FOR_APPROVAL)
                .cancelReason(CancelReason.INSUFFICIENT_STOCK)
                .build();

        Order order = mock(Order.class);
        when(order.getId()).thenReturn(new OrderId(orderId));
        when(order.getOrderStatus()).thenReturn(OrderStatus.WAITING_FOR_APPROVAL);

        OrderPaymentOutboxMessage po = OrderPaymentOutboxMessage.builder().sagaStatus(SagaStatus.PROCESSING).build();
        OrderApprovalOutboxMessage ao = OrderApprovalOutboxMessage.builder().sagaStatus(SagaStatus.PROCESSING).build();

        when(paymentOutboxHelper.getPaymentOutboxMessageBySagaId(orderId)).thenReturn(Optional.of(po));
        when(approvalOutboxHelper.getApprovalOutboxMessageBySagaId(orderId)).thenReturn(Optional.of(ao));

        helper.initCancelOrder(order, command);

        verify(order).initCancel(CancelReason.INSUFFICIENT_STOCK, MaterialAction.RECORD_WASTE, null);

        // AO_1 is sealed locally as COMPENSATED + COMPLETED
        assertEquals(OrderStatus.CANCELLING, ao.getOrderStatus());
        assertEquals(SagaStatus.COMPENSATED, ao.getSagaStatus());
        assertEquals(OutboxStatus.COMPLETED, ao.getOutboxStatus());
    }

    @Test
    @DisplayName("Matrix APPROVED: OPERATIONAL_DISRUPTIONS requires materialAction from client")
    void testApprovedOperationalRequiresMaterialAction() {
        UUID orderId = UUID.randomUUID();
        CancelOrderCommand command = CancelOrderCommand.builder()
                .orderId(orderId)
                .currentStatus(OrderStatus.APPROVED)
                .cancelReason(CancelReason.OPERATIONAL_DISRUPTIONS)
                .materialAction(null)
                .build();

        Order order = mock(Order.class);
        when(order.getOrderStatus()).thenReturn(OrderStatus.APPROVED);

        assertThrows(OrderDomainException.class, () -> helper.initCancelOrder(order, command));
    }

    @Test
    @DisplayName("Matrix APPROVED: INSUFFICIENT_STOCK rejects RESTORE_STOCK attempt")
    void testApprovedInsufficientStockRejectsRestoreStock() {
        UUID orderId = UUID.randomUUID();
        CancelOrderCommand command = CancelOrderCommand.builder()
                .orderId(orderId)
                .currentStatus(OrderStatus.APPROVED)
                .cancelReason(CancelReason.INSUFFICIENT_STOCK)
                .materialAction(MaterialAction.RESTORE_STOCK)
                .build();

        Order order = mock(Order.class);
        when(order.getOrderStatus()).thenReturn(OrderStatus.APPROVED);

        assertThrows(OrderDomainException.class, () -> helper.initCancelOrder(order, command));
    }

    @Test
    @DisplayName("Matrix READY_FOR_PICKUP: Rejects RESTORE_STOCK as cooked food cannot return to stock")
    void testReadyForPickupRejectsRestoreStock() {
        UUID orderId = UUID.randomUUID();
        CancelOrderCommand command = CancelOrderCommand.builder()
                .orderId(orderId)
                .currentStatus(OrderStatus.READY_FOR_PICKUP)
                .cancelReason(CancelReason.OPERATIONAL_DISRUPTIONS)
                .materialAction(MaterialAction.RESTORE_STOCK)
                .build();

        Order order = mock(Order.class);
        when(order.getOrderStatus()).thenReturn(OrderStatus.READY_FOR_PICKUP);

        assertThrows(OrderDomainException.class, () -> helper.initCancelOrder(order, command));
    }

    @Test
    @DisplayName("Matrix IN_DELIVERY: Customer fault (CUSTOMER_UNREACHABLE) triggers cancelWithoutRefund and local seals both outboxes (0 Kafka messages)")
    void testInDeliveryCustomerUnreachableZeroKafka() {
        UUID orderId = UUID.randomUUID();
        CancelOrderCommand command = CancelOrderCommand.builder()
                .orderId(orderId)
                .currentStatus(OrderStatus.IN_DELIVERY)
                .cancelReason(CancelReason.CUSTOMER_UNREACHABLE)
                .internalNote("15 mins waited at door")
                .build();

        Order order = mock(Order.class);
        when(order.getId()).thenReturn(new OrderId(orderId));
        when(order.getOrderStatus()).thenReturn(OrderStatus.IN_DELIVERY);

        OrderPaymentOutboxMessage po = OrderPaymentOutboxMessage.builder().sagaStatus(SagaStatus.PROCESSING).build();
        OrderApprovalOutboxMessage ao = OrderApprovalOutboxMessage.builder().sagaStatus(SagaStatus.PROCESSING).build();

        when(paymentOutboxHelper.getPaymentOutboxMessageBySagaId(orderId)).thenReturn(Optional.of(po));
        when(approvalOutboxHelper.getApprovalOutboxMessageBySagaId(orderId)).thenReturn(Optional.of(ao));

        helper.initCancelOrder(order, command);

        verify(orderDomainService).cancelOrderWithoutRefund(order, CancelReason.CUSTOMER_UNREACHABLE, MaterialAction.RECORD_WASTE, "15 mins waited at door");
        verify(orderRepository).save(order);

        // Both outboxes sealed in-place as COMPENSATED + COMPLETED
        assertEquals(OrderStatus.CANCELLED, po.getOrderStatus());
        assertEquals(SagaStatus.COMPENSATED, po.getSagaStatus());
        assertEquals(OutboxStatus.COMPLETED, po.getOutboxStatus());

        assertEquals(OrderStatus.CANCELLED, ao.getOrderStatus());
        assertEquals(SagaStatus.COMPENSATED, ao.getSagaStatus());
        assertEquals(OutboxStatus.COMPLETED, ao.getOutboxStatus());
    }

    @Test
    @DisplayName("Matrix IN_DELIVERY: Rejects kitchen reasons like INSUFFICIENT_STOCK")
    void testInDeliveryRejectsKitchenReasons() {
        UUID orderId = UUID.randomUUID();
        CancelOrderCommand command = CancelOrderCommand.builder()
                .orderId(orderId)
                .currentStatus(OrderStatus.IN_DELIVERY)
                .cancelReason(CancelReason.INSUFFICIENT_STOCK)
                .build();

        Order order = mock(Order.class);
        when(order.getOrderStatus()).thenReturn(OrderStatus.IN_DELIVERY);

        assertThrows(OrderDomainException.class, () -> helper.initCancelOrder(order, command));
    }
}
