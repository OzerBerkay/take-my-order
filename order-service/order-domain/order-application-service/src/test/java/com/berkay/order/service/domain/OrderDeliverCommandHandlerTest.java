package com.berkay.order.service.domain;

import com.berkay.domain.valueobject.OrderId;
import com.berkay.domain.valueobject.OrderStatus;
import com.berkay.order.service.domain.dto.operation.DeliverOrderCommand;
import com.berkay.order.service.domain.dto.restaurant.RestaurantOrderResponse;
import com.berkay.order.service.domain.entity.Order;
import com.berkay.order.service.domain.exception.OrderConflictException;
import com.berkay.order.service.domain.mapper.OrderDataMapper;
import com.berkay.order.service.domain.ports.output.repository.OrderRepository;
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
class OrderDeliverCommandHandlerTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private OrderDomainService orderDomainService;
    @Mock
    private OrderSagaHelper orderSagaHelper;
    @Mock
    private OrderDataMapper orderDataMapper;
    @Mock
    private com.berkay.order.service.domain.outbox.scheduler.delivery.DeliveryOutboxHelper deliveryOutboxHelper;

    private OrderDeliverCommandHandler handler;

    @BeforeEach
    void setUp() {
        handler = new OrderDeliverCommandHandler(orderRepository, orderDomainService, orderSagaHelper, orderDataMapper, deliveryOutboxHelper);
    }

    @Test
    @DisplayName("Successfully transitions to DELIVERED and seals SAGA as SUCCEEDED when order is in IN_DELIVERY status")
    void testDeliverSuccess() {
        UUID orderId = UUID.randomUUID();
        UUID restaurantId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        DeliverOrderCommand command = DeliverOrderCommand.builder().orderId(orderId).build();
        Order order = mock(Order.class);
        when(order.getId()).thenReturn(new OrderId(orderId));
        when(order.getRestaurantId()).thenReturn(new com.berkay.domain.valueobject.RestaurantId(restaurantId));
        when(order.getCustomerId()).thenReturn(new com.berkay.domain.valueobject.CustomerId(customerId));
        when(order.getPrice()).thenReturn(new com.berkay.domain.valueobject.Money(new java.math.BigDecimal("120.00")));
        when(order.getOrderStatus()).thenReturn(OrderStatus.IN_DELIVERY);
        when(orderRepository.findByIdWithLock(new OrderId(orderId))).thenReturn(Optional.of(order));

        RestaurantOrderResponse mockResponse = RestaurantOrderResponse.builder().orderId(orderId).orderStatus(OrderStatus.DELIVERED).build();
        when(orderDataMapper.orderToRestaurantOrderResponse(order)).thenReturn(mockResponse);

        RestaurantOrderResponse result = handler.deliverOrder(command);

        assertNotNull(result);
        assertEquals(OrderStatus.DELIVERED, result.getOrderStatus());
        verify(orderDomainService).deliver(order);
        verify(orderSagaHelper).sealSagaAsSucceeded(orderId);
        verify(orderRepository).save(order);
        verify(deliveryOutboxHelper).saveDeliveryOutboxMessage(any(), eq(com.berkay.outbox.OutboxStatus.STARTED));
    }

    @Test
    @DisplayName("Throws OrderConflictException when order is not in IN_DELIVERY status")
    void testDeliverConflict() {
        UUID orderId = UUID.randomUUID();
        DeliverOrderCommand command = DeliverOrderCommand.builder().orderId(orderId).build();
        Order order = mock(Order.class);
        when(order.getId()).thenReturn(new OrderId(orderId));
        when(order.getOrderStatus()).thenReturn(OrderStatus.READY_FOR_PICKUP);
        when(orderRepository.findByIdWithLock(new OrderId(orderId))).thenReturn(Optional.of(order));

        assertThrows(OrderConflictException.class, () -> handler.deliverOrder(command));
        verify(orderDomainService, never()).deliver(any());
        verify(orderSagaHelper, never()).sealSagaAsSucceeded(any());
        verify(orderRepository, never()).save(any());
    }
}
