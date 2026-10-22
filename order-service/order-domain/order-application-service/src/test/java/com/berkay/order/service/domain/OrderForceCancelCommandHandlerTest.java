package com.berkay.order.service.domain;

import com.berkay.domain.valueobject.*;
import com.berkay.order.service.domain.dto.operation.ForceCancelOrderCommand;
import com.berkay.order.service.domain.dto.restaurant.RestaurantOrderResponse;
import com.berkay.order.service.domain.entity.Order;
import com.berkay.order.service.domain.exception.OrderConflictException;
import com.berkay.order.service.domain.mapper.OrderDataMapper;
import com.berkay.order.service.domain.outbox.model.approval.OrderApprovalOutboxMessage;
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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderForceCancelCommandHandlerTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private PaymentOutboxHelper paymentOutboxHelper;
    @Mock
    private ApprovalOutboxHelper approvalOutboxHelper;
    @Mock
    private OrderCancelHelper orderCancelHelper;
    @Mock
    private OrderDataMapper orderDataMapper;

    private OrderForceCancelCommandHandler handler;

    @BeforeEach
    void setUp() {
        handler = new OrderForceCancelCommandHandler(orderRepository, paymentOutboxHelper,
                approvalOutboxHelper, orderCancelHelper, orderDataMapper);
    }

    @Test
    @DisplayName("Protected state DELIVERED rejects force cancel with OrderConflictException")
    void testForceCancelRejectsDelivered() {
        UUID orderId = UUID.randomUUID();
        ForceCancelOrderCommand command = ForceCancelOrderCommand.builder()
                .orderId(orderId)
                .adminReason("Customer complained after 2 days")
                .forceRefund(false)
                .build();

        Order order = mock(Order.class);
        when(order.getId()).thenReturn(new OrderId(orderId));
        when(order.getOrderStatus()).thenReturn(OrderStatus.DELIVERED);
        when(orderRepository.findByIdWithLock(new OrderId(orderId))).thenReturn(Optional.of(order));

        assertThrows(OrderConflictException.class, () -> handler.forceCancelOrder(command));
    }

    @Test
    @DisplayName("Live state APPROVED with forceRefund=true re-arms both outboxes with RESTORE_STOCK")
    void testForceCancelApprovedWithRefund() {
        UUID orderId = UUID.randomUUID();
        ForceCancelOrderCommand command = ForceCancelOrderCommand.builder()
                .orderId(orderId)
                .adminReason("Customer phone unreachable and kitchen confirmed idle")
                .forceRefund(true)
                .build();

        Order order = mock(Order.class);
        when(order.getId()).thenReturn(new OrderId(orderId));
        when(order.getOrderStatus()).thenReturn(OrderStatus.APPROVED);
        when(orderRepository.findByIdWithLock(new OrderId(orderId))).thenReturn(Optional.of(order));

        OrderPaymentOutboxMessage po = OrderPaymentOutboxMessage.builder().sagaStatus(SagaStatus.PROCESSING).build();
        OrderApprovalOutboxMessage ao = OrderApprovalOutboxMessage.builder().sagaStatus(SagaStatus.PROCESSING).build();

        when(paymentOutboxHelper.getPaymentOutboxMessageBySagaId(orderId)).thenReturn(Optional.of(po));
        when(approvalOutboxHelper.getApprovalOutboxMessageBySagaId(orderId)).thenReturn(Optional.of(ao));
        when(orderCancelHelper.createPaymentPayload(any())).thenReturn("{}");
        when(orderCancelHelper.createApprovalPayload(any())).thenReturn("{}");

        RestaurantOrderResponse mockResponse = RestaurantOrderResponse.builder().orderId(orderId).orderStatus(OrderStatus.CANCELLING).build();
        when(orderDataMapper.orderToRestaurantOrderResponse(order)).thenReturn(mockResponse);

        RestaurantOrderResponse result = handler.forceCancelOrder(command);

        assertNotNull(result);
        verify(order).initCancel(CancelReason.OPERATIONAL_DISRUPTIONS, MaterialAction.RESTORE_STOCK,
                "[ADMIN FORCE CANCEL] Customer phone unreachable and kitchen confirmed idle");
        assertEquals(SagaStatus.COMPENSATING, po.getSagaStatus());
        assertEquals(OutboxStatus.STARTED, po.getOutboxStatus());

        assertEquals(SagaStatus.COMPENSATING, ao.getSagaStatus());
        assertEquals(OutboxStatus.STARTED, ao.getOutboxStatus());
    }

    @Test
    @DisplayName("Live state IN_DELIVERY with forceRefund=false seals both outboxes and immediately finalizes order as CANCELLED")
    void testForceCancelInDeliveryWithoutRefundImmediateFinalize() {
        UUID orderId = UUID.randomUUID();
        ForceCancelOrderCommand command = ForceCancelOrderCommand.builder()
                .orderId(orderId)
                .adminReason("Call center emergency closure without refund")
                .forceRefund(false)
                .build();

        Order order = mock(Order.class);
        when(order.getId()).thenReturn(new OrderId(orderId));
        when(order.getOrderStatus()).thenReturn(OrderStatus.IN_DELIVERY);
        when(order.getCancelReason()).thenReturn(CancelReason.OPERATIONAL_DISRUPTIONS);
        when(orderRepository.findByIdWithLock(new OrderId(orderId))).thenReturn(Optional.of(order));

        OrderPaymentOutboxMessage po = OrderPaymentOutboxMessage.builder().sagaStatus(SagaStatus.PROCESSING).build();
        OrderApprovalOutboxMessage ao = OrderApprovalOutboxMessage.builder().sagaStatus(SagaStatus.PROCESSING).build();

        when(paymentOutboxHelper.getPaymentOutboxMessageBySagaId(orderId)).thenReturn(Optional.of(po));
        when(approvalOutboxHelper.getApprovalOutboxMessageBySagaId(orderId)).thenReturn(Optional.of(ao));

        RestaurantOrderResponse mockResponse = RestaurantOrderResponse.builder().orderId(orderId).orderStatus(OrderStatus.CANCELLED).build();
        when(orderDataMapper.orderToRestaurantOrderResponse(order)).thenReturn(mockResponse);

        RestaurantOrderResponse result = handler.forceCancelOrder(command);

        assertNotNull(result);
        // Calculated action for IN_DELIVERY is RECORD_WASTE
        verify(order).initCancel(CancelReason.OPERATIONAL_DISRUPTIONS, MaterialAction.RECORD_WASTE,
                "[ADMIN FORCE CANCEL] Call center emergency closure without refund");
        // Because forceRefund=false and RECORD_WASTE, both are locally COMPENSATED and order is finalized:
        verify(order).cancel(CancelReason.OPERATIONAL_DISRUPTIONS);
        assertEquals(OrderStatus.CANCELLED, po.getOrderStatus());
        assertEquals(SagaStatus.COMPENSATED, po.getSagaStatus());
        assertEquals(OutboxStatus.COMPLETED, po.getOutboxStatus());

        assertEquals(OrderStatus.CANCELLED, ao.getOrderStatus());
        assertEquals(SagaStatus.COMPENSATED, ao.getSagaStatus());
        assertEquals(OutboxStatus.COMPLETED, ao.getOutboxStatus());
    }
}
