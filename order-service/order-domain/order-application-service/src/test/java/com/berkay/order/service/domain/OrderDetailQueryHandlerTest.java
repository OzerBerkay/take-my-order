package com.berkay.order.service.domain;

import com.berkay.domain.valueobject.OrderId;
import com.berkay.domain.valueobject.OrderStatus;
import com.berkay.order.service.domain.dto.restaurant.OrderDetailQuery;
import com.berkay.order.service.domain.dto.restaurant.RestaurantOrderResponse;
import com.berkay.order.service.domain.entity.Order;
import com.berkay.order.service.domain.exception.OrderNotFoundException;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderDetailQueryHandlerTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private OrderDataMapper orderDataMapper;

    private OrderDetailQueryHandler handler;

    @BeforeEach
    void setUp() {
        handler = new OrderDetailQueryHandler(orderRepository, orderDataMapper);
    }

    @Test
    @DisplayName("Successfully returns single order detail")
    void testGetOrderDetailSuccess() {
        UUID orderId = UUID.randomUUID();
        OrderDetailQuery query = OrderDetailQuery.builder().orderId(orderId).build();
        Order order = mock(Order.class);

        when(orderRepository.findById(new OrderId(orderId))).thenReturn(Optional.of(order));

        RestaurantOrderResponse response = RestaurantOrderResponse.builder()
                .orderId(orderId)
                .orderStatus(OrderStatus.PREPARING)
                .build();
        when(orderDataMapper.orderToRestaurantOrderResponse(order)).thenReturn(response);

        RestaurantOrderResponse result = handler.getOrderDetail(query);

        assertNotNull(result);
        assertEquals(orderId, result.getOrderId());
        assertEquals(OrderStatus.PREPARING, result.getOrderStatus());
    }

    @Test
    @DisplayName("Throws OrderNotFoundException when order does not exist")
    void testGetOrderDetailNotFound() {
        UUID orderId = UUID.randomUUID();
        OrderDetailQuery query = OrderDetailQuery.builder().orderId(orderId).build();

        when(orderRepository.findById(new OrderId(orderId))).thenReturn(Optional.empty());

        assertThrows(OrderNotFoundException.class, () -> handler.getOrderDetail(query));
    }
}
