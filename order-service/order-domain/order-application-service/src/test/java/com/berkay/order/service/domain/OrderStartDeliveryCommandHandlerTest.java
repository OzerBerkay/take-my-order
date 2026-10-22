package com.berkay.order.service.domain;

import com.berkay.domain.valueobject.OrderId;
import com.berkay.domain.valueobject.OrderStatus;
import com.berkay.order.service.domain.dto.operation.StartDeliveryOrderCommand;
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
class OrderStartDeliveryCommandHandlerTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private OrderDomainService orderDomainService;
    @Mock
    private OrderDataMapper orderDataMapper;

    private OrderStartDeliveryCommandHandler handler;

    @BeforeEach
    void setUp() {
        handler = new OrderStartDeliveryCommandHandler(orderRepository, orderDomainService, orderDataMapper);
    }

    @Test
    @DisplayName("Successfully transitions to IN_DELIVERY when order is in READY_FOR_PICKUP status")
    void testStartDeliverySuccess() {
        UUID orderId = UUID.randomUUID();
        StartDeliveryOrderCommand command = StartDeliveryOrderCommand.builder().orderId(orderId).build();
        Order order = mock(Order.class);
        when(order.getId()).thenReturn(new OrderId(orderId));
        when(order.getOrderStatus()).thenReturn(OrderStatus.READY_FOR_PICKUP);
        when(orderRepository.findByIdWithLock(new OrderId(orderId))).thenReturn(Optional.of(order));

        RestaurantOrderResponse mockResponse = RestaurantOrderResponse.builder().orderId(orderId).orderStatus(OrderStatus.IN_DELIVERY).build();
        when(orderDataMapper.orderToRestaurantOrderResponse(order)).thenReturn(mockResponse);

        RestaurantOrderResponse result = handler.startDeliveryOrder(command);

        assertNotNull(result);
        assertEquals(OrderStatus.IN_DELIVERY, result.getOrderStatus());
        verify(orderDomainService).startDelivery(order);
        verify(orderRepository).save(order);
    }

    @Test
    @DisplayName("Throws OrderConflictException when order is not in READY_FOR_PICKUP status")
    void testStartDeliveryConflict() {
        UUID orderId = UUID.randomUUID();
        StartDeliveryOrderCommand command = StartDeliveryOrderCommand.builder().orderId(orderId).build();
        Order order = mock(Order.class);
        when(order.getId()).thenReturn(new OrderId(orderId));
        when(order.getOrderStatus()).thenReturn(OrderStatus.PREPARING);
        when(orderRepository.findByIdWithLock(new OrderId(orderId))).thenReturn(Optional.of(order));

        assertThrows(OrderConflictException.class, () -> handler.startDeliveryOrder(command));
        verify(orderDomainService, never()).startDelivery(any());
        verify(orderRepository, never()).save(any());
    }
}
