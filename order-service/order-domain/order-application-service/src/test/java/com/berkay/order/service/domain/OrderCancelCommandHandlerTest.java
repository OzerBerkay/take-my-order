package com.berkay.order.service.domain;

import com.berkay.domain.valueobject.CancelReason;
import com.berkay.domain.valueobject.OrderId;
import com.berkay.domain.valueobject.OrderStatus;
import com.berkay.order.service.domain.dto.operation.CancelOrderCommand;
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
class OrderCancelCommandHandlerTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private OrderCancelHelper orderCancelHelper;
    @Mock
    private OrderDataMapper orderDataMapper;

    private OrderCancelCommandHandler handler;

    @BeforeEach
    void setUp() {
        handler = new OrderCancelCommandHandler(orderRepository, orderCancelHelper, orderDataMapper);
    }

    @Test
    @DisplayName("Idempotent HTTP 200 return when order is already in CANCELLING state (double-click safe)")
    void testCancelOrderIdempotentWhenAlreadyCancelling() {
        UUID orderId = UUID.randomUUID();
        CancelOrderCommand command = CancelOrderCommand.builder()
                .orderId(orderId)
                .currentStatus(OrderStatus.APPROVED)
                .cancelReason(CancelReason.OPERATIONAL_DISRUPTIONS)
                .build();

        Order order = mock(Order.class);
        when(order.getId()).thenReturn(new OrderId(orderId));
        when(order.getOrderStatus()).thenReturn(OrderStatus.CANCELLING);
        when(orderRepository.findByIdWithLock(new OrderId(orderId))).thenReturn(Optional.of(order));

        RestaurantOrderResponse mockResponse = RestaurantOrderResponse.builder()
                .orderId(orderId)
                .orderStatus(OrderStatus.CANCELLING)
                .build();
        when(orderDataMapper.orderToRestaurantOrderResponse(order)).thenReturn(mockResponse);

        RestaurantOrderResponse result = handler.cancelOrder(command);

        assertNotNull(result);
        assertEquals(OrderStatus.CANCELLING, result.getOrderStatus());
        verify(orderCancelHelper, never()).initCancelOrder(any(), any());
    }

    @Test
    @DisplayName("Idempotent HTTP 200 return when order is already in CANCELLED state")
    void testCancelOrderIdempotentWhenAlreadyCancelled() {
        UUID orderId = UUID.randomUUID();
        CancelOrderCommand command = CancelOrderCommand.builder()
                .orderId(orderId)
                .currentStatus(OrderStatus.APPROVED)
                .cancelReason(CancelReason.OPERATIONAL_DISRUPTIONS)
                .build();

        Order order = mock(Order.class);
        when(order.getId()).thenReturn(new OrderId(orderId));
        when(order.getOrderStatus()).thenReturn(OrderStatus.CANCELLED);
        when(orderRepository.findByIdWithLock(new OrderId(orderId))).thenReturn(Optional.of(order));

        RestaurantOrderResponse mockResponse = RestaurantOrderResponse.builder()
                .orderId(orderId)
                .orderStatus(OrderStatus.CANCELLED)
                .build();
        when(orderDataMapper.orderToRestaurantOrderResponse(order)).thenReturn(mockResponse);

        RestaurantOrderResponse result = handler.cancelOrder(command);

        assertNotNull(result);
        assertEquals(OrderStatus.CANCELLED, result.getOrderStatus());
        verify(orderCancelHelper, never()).initCancelOrder(any(), any());
    }

    @Test
    @DisplayName("Throws OrderConflictException when client currentStatus does not match DB status")
    void testCancelOrderConflictWhenStatusMismatch() {
        UUID orderId = UUID.randomUUID();
        CancelOrderCommand command = CancelOrderCommand.builder()
                .orderId(orderId)
                .currentStatus(OrderStatus.APPROVED)
                .cancelReason(CancelReason.OPERATIONAL_DISRUPTIONS)
                .build();

        Order order = mock(Order.class);
        when(order.getId()).thenReturn(new OrderId(orderId));
        when(order.getOrderStatus()).thenReturn(OrderStatus.PREPARING); // already advanced
        when(orderRepository.findByIdWithLock(new OrderId(orderId))).thenReturn(Optional.of(order));

        assertThrows(OrderConflictException.class, () -> handler.cancelOrder(command));
        verify(orderCancelHelper, never()).initCancelOrder(any(), any());
    }

    @Test
    @DisplayName("Successfully delegates to orderCancelHelper on matching status")
    void testCancelOrderSuccess() {
        UUID orderId = UUID.randomUUID();
        CancelOrderCommand command = CancelOrderCommand.builder()
                .orderId(orderId)
                .currentStatus(OrderStatus.APPROVED)
                .cancelReason(CancelReason.OPERATIONAL_DISRUPTIONS)
                .build();

        Order order = mock(Order.class);
        when(order.getOrderStatus()).thenReturn(OrderStatus.APPROVED);
        when(orderRepository.findByIdWithLock(new OrderId(orderId))).thenReturn(Optional.of(order));

        RestaurantOrderResponse mockResponse = RestaurantOrderResponse.builder()
                .orderId(orderId)
                .orderStatus(OrderStatus.CANCELLING)
                .build();
        when(orderDataMapper.orderToRestaurantOrderResponse(order)).thenReturn(mockResponse);

        RestaurantOrderResponse result = handler.cancelOrder(command);

        assertNotNull(result);
        verify(orderCancelHelper).initCancelOrder(order, command);
    }
}
