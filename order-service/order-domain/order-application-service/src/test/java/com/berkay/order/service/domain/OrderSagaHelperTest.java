package com.berkay.order.service.domain;

import com.berkay.domain.valueobject.OrderStatus;
import com.berkay.order.service.domain.ports.output.repository.OrderRepository;
import com.berkay.saga.SagaStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class OrderSagaHelperTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private com.berkay.order.service.domain.outbox.scheduler.payment.PaymentOutboxHelper paymentOutboxHelper;

    @Mock
    private com.berkay.order.service.domain.outbox.scheduler.approval.ApprovalOutboxHelper approvalOutboxHelper;

    private OrderSagaHelper orderSagaHelper;

    @BeforeEach
    void setUp() {
        orderSagaHelper = new OrderSagaHelper(orderRepository, paymentOutboxHelper, approvalOutboxHelper);
    }

    @Test
    @DisplayName("Verify 10-state OrderStatus to SagaStatus mapping contract")
    void testOrderStatusToSagaStatusMapping() {
        // PENDING -> STARTED
        assertEquals(SagaStatus.STARTED, orderSagaHelper.orderStatusToSagaStatus(OrderStatus.PENDING));

        // Interim states -> PROCESSING
        assertEquals(SagaStatus.PROCESSING, orderSagaHelper.orderStatusToSagaStatus(OrderStatus.PAID));
        assertEquals(SagaStatus.PROCESSING, orderSagaHelper.orderStatusToSagaStatus(OrderStatus.WAITING_FOR_APPROVAL));
        assertEquals(SagaStatus.PROCESSING, orderSagaHelper.orderStatusToSagaStatus(OrderStatus.APPROVED));
        assertEquals(SagaStatus.PROCESSING, orderSagaHelper.orderStatusToSagaStatus(OrderStatus.PREPARING));
        assertEquals(SagaStatus.PROCESSING, orderSagaHelper.orderStatusToSagaStatus(OrderStatus.READY_FOR_PICKUP));
        assertEquals(SagaStatus.PROCESSING, orderSagaHelper.orderStatusToSagaStatus(OrderStatus.IN_DELIVERY));

        // Terminal success -> SUCCEEDED (ONLY at doorstep delivery)
        assertEquals(SagaStatus.SUCCEEDED, orderSagaHelper.orderStatusToSagaStatus(OrderStatus.DELIVERED));

        // Compensating states
        assertEquals(SagaStatus.COMPENSATING, orderSagaHelper.orderStatusToSagaStatus(OrderStatus.CANCELLING));
        assertEquals(SagaStatus.COMPENSATED, orderSagaHelper.orderStatusToSagaStatus(OrderStatus.CANCELLED));
    }

    @Test
    @DisplayName("Verify sealing saga as SUCCEEDED updates both outbox records")
    void testSealSagaAsSucceeded() {
        java.util.UUID orderId = java.util.UUID.randomUUID();
        com.berkay.order.service.domain.outbox.model.payment.OrderPaymentOutboxMessage po =
                com.berkay.order.service.domain.outbox.model.payment.OrderPaymentOutboxMessage.builder()
                        .sagaStatus(SagaStatus.PROCESSING)
                        .orderStatus(OrderStatus.IN_DELIVERY)
                        .build();
        com.berkay.order.service.domain.outbox.model.approval.OrderApprovalOutboxMessage ao =
                com.berkay.order.service.domain.outbox.model.approval.OrderApprovalOutboxMessage.builder()
                        .sagaStatus(SagaStatus.PROCESSING)
                        .orderStatus(OrderStatus.IN_DELIVERY)
                        .build();

        org.mockito.Mockito.when(paymentOutboxHelper.getPaymentOutboxMessageBySagaId(orderId))
                .thenReturn(java.util.Optional.of(po));
        org.mockito.Mockito.when(approvalOutboxHelper.getApprovalOutboxMessageBySagaId(orderId))
                .thenReturn(java.util.Optional.of(ao));

        orderSagaHelper.sealSagaAsSucceeded(orderId);

        assertEquals(SagaStatus.SUCCEEDED, po.getSagaStatus());
        assertEquals(OrderStatus.DELIVERED, po.getOrderStatus());
        assertEquals(SagaStatus.SUCCEEDED, ao.getSagaStatus());
        assertEquals(OrderStatus.DELIVERED, ao.getOrderStatus());
        org.mockito.Mockito.verify(paymentOutboxHelper).save(po);
        org.mockito.Mockito.verify(approvalOutboxHelper).save(ao);
    }
}
