package com.berkay.order.service.domain;

import com.berkay.domain.valueobject.OrderId;
import com.berkay.domain.valueobject.OrderStatus;
import com.berkay.order.service.domain.dto.operation.ReadyForPickupOrderCommand;
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
class OrderReadyForPickupCommandHandlerTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private OrderDomainService orderDomainService;
    @Mock
    private OrderDataMapper orderDataMapper;

    private OrderReadyForPickupCommandHandler handler;

    @BeforeEach
    void setUp() {
        handler = new OrderReadyForPickupCommandHandler(orderRepository, orderDomainService, orderDataMapper);
    }

    @Test
    @DisplayName("Successfully transitions to READY_FOR_PICKUP when order is in PREPARING status")
    void testReadyForPickupSuccess() {
        UUID orderId = UUID.randomUUID();
        ReadyForPickupOrderCommand command = ReadyForPickupOrderCommand.builder().orderId(orderId).build();
        Order order = mock(Order.class);
        when(order.getId()).thenReturn(new OrderId(orderId));
        when(order.getOrderStatus()).thenReturn(OrderStatus.PREPARING);
        when(orderRepository.findByIdWithLock(new OrderId(orderId))).thenReturn(Optional.of(order));

        RestaurantOrderResponse mockResponse = RestaurantOrderResponse.builder().orderId(orderId).orderStatus(OrderStatus.READY_FOR_PICKUP).build();
        when(orderDataMapper.orderToRestaurantOrderResponse(order)).thenReturn(mockResponse);

        RestaurantOrderResponse result = handler.readyForPickupOrder(command);

        assertNotNull(result);
        assertEquals(OrderStatus.READY_FOR_PICKUP, result.getOrderStatus());
        verify(orderDomainService).readyForPickup(order);
        verify(orderRepository).save(order);
    }

    @Test
    @DisplayName("Throws OrderConflictException when order is not in PREPARING status")
    void testReadyForPickupConflict() {
        UUID orderId = UUID.randomUUID();
        ReadyForPickupOrderCommand command = ReadyForPickupOrderCommand.builder().orderId(orderId).build();
        Order order = mock(Order.class);
        when(order.getId()).thenReturn(new OrderId(orderId));
        when(order.getOrderStatus()).thenReturn(OrderStatus.APPROVED);
        when(orderRepository.findByIdWithLock(new OrderId(orderId))).thenReturn(Optional.of(order));

        assertThrows(OrderConflictException.class, () -> handler.readyForPickupOrder(command));
        verify(orderDomainService, never()).readyForPickup(any());
        verify(orderRepository, never()).save(any());
    }
}
