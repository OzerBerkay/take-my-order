package com.berkay.order.service.domain;

import com.berkay.domain.valueobject.OrderStatus;
import com.berkay.domain.valueobject.RestaurantId;
import com.berkay.order.service.domain.dto.read.OrderPageResult;
import com.berkay.order.service.domain.dto.restaurant.RestaurantOrderResponse;
import com.berkay.order.service.domain.dto.restaurant.RestaurantOrdersPageResponse;
import com.berkay.order.service.domain.dto.restaurant.RestaurantOrdersQuery;
import com.berkay.order.service.domain.entity.Order;
import com.berkay.order.service.domain.exception.OrderDomainException;
import com.berkay.order.service.domain.mapper.OrderDataMapper;
import com.berkay.order.service.domain.ports.output.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RestaurantOrdersQueryHandlerTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private OrderDataMapper orderDataMapper;

    private RestaurantOrdersQueryHandler handler;

    @BeforeEach
    void setUp() {
        handler = new RestaurantOrdersQueryHandler(orderRepository, orderDataMapper);
    }

    @Test
    @DisplayName("Search query with less than 3 chars throws OrderDomainException")
    void testSearchQueryTooShort() {
        RestaurantOrdersQuery query = RestaurantOrdersQuery.builder()
                .restaurantId(UUID.randomUUID())
                .search("ab")
                .build();

        assertThrows(OrderDomainException.class, () -> handler.getRestaurantOrders(query));
        verify(orderRepository, never()).findRestaurantOrders(any(), any(), any(), anyInt(), anyInt(), any());
    }

    @Test
    @DisplayName("Successfully returns paginated RestaurantOrdersPageResponse")
    void testGetRestaurantOrdersSuccess() {
        UUID restaurantId = UUID.randomUUID();
        RestaurantOrdersQuery query = RestaurantOrdersQuery.builder()
                .restaurantId(restaurantId)
                .status(OrderStatus.APPROVED)
                .search("burger")
                .page(0)
                .size(10)
                .sort("createdAt,asc")
                .build();

        Order order = mock(Order.class);
        OrderPageResult pageResult = OrderPageResult.builder()
                .orders(List.of(order))
                .page(0)
                .size(10)
                .totalElements(1L)
                .totalPages(1)
                .isLast(true)
                .build();

        when(orderRepository.findRestaurantOrders(eq(new RestaurantId(restaurantId)), eq(OrderStatus.APPROVED),
                eq("burger"), eq(0), eq(10), eq("createdAt,asc")))
                .thenReturn(pageResult);

        RestaurantOrderResponse response = RestaurantOrderResponse.builder()
                .orderId(UUID.randomUUID())
                .restaurantId(restaurantId)
                .orderStatus(OrderStatus.APPROVED)
                .build();
        when(orderDataMapper.orderToRestaurantOrderResponse(order)).thenReturn(response);

        RestaurantOrdersPageResponse result = handler.getRestaurantOrders(query);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(1L, result.getTotalElements());
        assertEquals(1, result.getTotalPages());
        assertTrue(result.isLast());
    }
}
