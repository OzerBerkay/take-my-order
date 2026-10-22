package com.berkay.order.service.domain;

import com.berkay.domain.valueobject.OrderId;
import com.berkay.domain.valueobject.OrderStatus;
import com.berkay.order.service.domain.dto.operation.StartPreparingOrderCommand;
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
class OrderStartPreparingCommandHandlerTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private OrderDomainService orderDomainService;
    @Mock
    private OrderDataMapper orderDataMapper;

    private OrderStartPreparingCommandHandler handler;

    @BeforeEach
    void setUp() {
        handler = new OrderStartPreparingCommandHandler(orderRepository, orderDomainService, orderDataMapper);
    }

    @Test
    @DisplayName("Successfully starts preparing when order is in APPROVED status")
    void testStartPreparingSuccess() {
        UUID orderId = UUID.randomUUID();
        StartPreparingOrderCommand command = StartPreparingOrderCommand.builder().orderId(orderId).build();
        Order order = mock(Order.class);
        when(order.getId()).thenReturn(new OrderId(orderId));
        when(order.getOrderStatus()).thenReturn(OrderStatus.APPROVED);
        when(orderRepository.findByIdWithLock(new OrderId(orderId))).thenReturn(Optional.of(order));

        RestaurantOrderResponse mockResponse = RestaurantOrderResponse.builder().orderId(orderId).orderStatus(OrderStatus.PREPARING).build();
        when(orderDataMapper.orderToRestaurantOrderResponse(order)).thenReturn(mockResponse);

        RestaurantOrderResponse result = handler.startPreparingOrder(command);

        assertNotNull(result);
        assertEquals(OrderStatus.PREPARING, result.getOrderStatus());
        verify(orderDomainService).startPreparing(order);
        verify(orderRepository).save(order);
    }

    @Test
    @DisplayName("Throws OrderConflictException when order is not in APPROVED status")
    void testStartPreparingConflict() {
        UUID orderId = UUID.randomUUID();
        StartPreparingOrderCommand command = StartPreparingOrderCommand.builder().orderId(orderId).build();
        Order order = mock(Order.class);
        when(order.getId()).thenReturn(new OrderId(orderId));
        when(order.getOrderStatus()).thenReturn(OrderStatus.WAITING_FOR_APPROVAL);
        when(orderRepository.findByIdWithLock(new OrderId(orderId))).thenReturn(Optional.of(order));

        assertThrows(OrderConflictException.class, () -> handler.startPreparingOrder(command));
        verify(orderDomainService, never()).startPreparing(any());
        verify(orderRepository, never()).save(any());
    }
}
