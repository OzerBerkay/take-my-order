package com.berkay.order.service.domain;

import com.berkay.domain.valueobject.CustomerId;
import com.berkay.domain.valueobject.OrderId;
import com.berkay.domain.valueobject.OrderStatus;
import com.berkay.order.service.domain.dto.read.*;
import com.berkay.order.service.domain.entity.Order;
import com.berkay.order.service.domain.entity.OrderItem;
import com.berkay.order.service.domain.entity.Product;
import com.berkay.order.service.domain.entity.Restaurant;
import com.berkay.order.service.domain.exception.OrderNotFoundException;
import com.berkay.order.service.domain.mapper.OrderDataMapper;
import com.berkay.order.service.domain.ports.output.repository.OrderRepository;
import com.berkay.order.service.domain.ports.output.repository.RestaurantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerOrderQueryHandlerTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private RestaurantRepository restaurantRepository;
    @Mock
    private OrderDataMapper orderDataMapper;

    private CustomerOrderQueryHandler handler;

    @BeforeEach
    void setUp() {
        handler = new CustomerOrderQueryHandler(orderRepository, restaurantRepository, orderDataMapper);
    }

    @Test
    @DisplayName("Successfully returns customer order detail")
    void testGetOrderDetailSuccess() {
        UUID orderId = UUID.randomUUID();
        UUID restaurantId = UUID.randomUUID();
        CustomerOrderDetailQuery query = CustomerOrderDetailQuery.builder().orderId(orderId).build();
        Order order = mock(Order.class);

        when(order.getRestaurantId()).thenReturn(new com.berkay.domain.valueobject.RestaurantId(restaurantId));
        when(orderRepository.findById(new OrderId(orderId))).thenReturn(Optional.of(order));

        Restaurant restaurant = mock(Restaurant.class);
        when(restaurant.getName()).thenReturn("Burger King");
        when(restaurantRepository.findRestaurantByRestaurantId(restaurantId)).thenReturn(Optional.of(restaurant));

        CustomerOrderDetailResponse response = CustomerOrderDetailResponse.builder()
                .orderId(orderId)
                .restaurantName("Burger King")
                .orderStatus(OrderStatus.PREPARING)
                .price(new BigDecimal("150.00"))
                .build();
        when(orderDataMapper.orderToCustomerOrderDetailResponse(order, "Burger King")).thenReturn(response);

        CustomerOrderDetailResponse result = handler.getOrderDetail(query);

        assertNotNull(result);
        assertEquals(orderId, result.getOrderId());
        assertEquals("Burger King", result.getRestaurantName());
        assertEquals(OrderStatus.PREPARING, result.getOrderStatus());
        assertEquals(new BigDecimal("150.00"), result.getPrice());
    }

    @Test
    @DisplayName("Throws OrderNotFoundException when order does not exist")
    void testGetOrderDetailNotFound() {
        UUID orderId = UUID.randomUUID();
        CustomerOrderDetailQuery query = CustomerOrderDetailQuery.builder().orderId(orderId).build();

        when(orderRepository.findById(new OrderId(orderId))).thenReturn(Optional.empty());

        assertThrows(OrderNotFoundException.class, () -> handler.getOrderDetail(query));
        verify(orderDataMapper, never()).orderToCustomerOrderDetailResponse(any(), any());
    }

    @Test
    @DisplayName("Successfully returns customer orders page with enriched fields and badge counts")
    void testGetOrdersSuccess() {
        UUID customerId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        UUID restaurantId = UUID.randomUUID();
        UUID trackingId = UUID.randomUUID();

        GetOrdersQuery query = GetOrdersQuery.builder()
                .customerId(customerId)
                .page(0)
                .size(10)
                .filter(OrderFilterGroup.ALL)
                .build();

        OrderItem item1 = mock(OrderItem.class);
        Product product1 = mock(Product.class);
        when(product1.getName()).thenReturn("Whopper Menü");
        when(item1.getProduct()).thenReturn(product1);
        when(item1.getQuantity()).thenReturn(2);

        Order order = mock(Order.class);
        when(order.getId()).thenReturn(new OrderId(orderId));
        when(order.getTrackingId()).thenReturn(new com.berkay.order.service.domain.valueobject.TrackingId(trackingId));
        when(order.getRestaurantId()).thenReturn(new com.berkay.domain.valueobject.RestaurantId(restaurantId));
        when(order.getOrderStatus()).thenReturn(OrderStatus.APPROVED);
        when(order.getPrice()).thenReturn(new com.berkay.domain.valueobject.Money(new BigDecimal("100.00")));
        when(order.getCancelReason()).thenReturn(null);
        when(order.getFailureMessages()).thenReturn(null);
        when(order.getCreatedAt()).thenReturn(java.time.ZonedDateTime.now());
        when(order.getItems()).thenReturn(List.of(item1));

        OrderPageResult pageResult = new OrderPageResult(List.of(order), 0, 10, 1L, 1, true);
        when(orderRepository.findByCustomerId(new CustomerId(customerId), 0, 10)).thenReturn(pageResult);
        when(restaurantRepository.findRestaurantNamesByIds(List.of(restaurantId))).thenReturn(Map.of(restaurantId, "Burger King"));

        com.berkay.order.service.domain.ports.output.repository.CustomerOrdersSummaryProjection summaryProjection =
                mock(com.berkay.order.service.domain.ports.output.repository.CustomerOrdersSummaryProjection.class);
        when(summaryProjection.getAllCount()).thenReturn(10L);
        when(summaryProjection.getActiveCount()).thenReturn(2L);
        when(summaryProjection.getSucceededCount()).thenReturn(7L);
        when(summaryProjection.getAbortCount()).thenReturn(1L);
        when(orderRepository.getCustomerOrdersSummary(new CustomerId(customerId))).thenReturn(summaryProjection);

        GetOrdersResponse response = handler.getOrders(query);

        assertNotNull(response);
        assertEquals(1, response.getOrders().size());
        assertEquals(1L, response.getTotalElements());
        assertTrue(response.isLast());
        assertEquals(10L, response.getAllCount());
        assertEquals(2L, response.getActiveCount());
        assertEquals(7L, response.getSucceededCount());
        assertEquals(1L, response.getAbortCount());

        OrderSummary summary = response.getOrders().get(0);
        assertEquals(orderId, summary.getOrderId());
        assertEquals(trackingId, summary.getOrderTrackingId());
        assertEquals(restaurantId, summary.getRestaurantId());
        assertEquals("Burger King", summary.getRestaurantName());
        assertEquals(OrderStatus.APPROVED, summary.getOrderStatus());
        assertEquals(new BigDecimal("100.00"), summary.getTotalAmount());
        assertEquals(2, summary.getTotalItemCount());
        assertEquals(1, summary.getItems().size());
        assertEquals("Whopper Menü", summary.getItems().get(0).getName());
        assertEquals(2, summary.getItems().get(0).getQuantity());
    }

    @Test
    @DisplayName("Successfully queries customer orders with ACTIVE filter")
    void testGetOrdersWithActiveFilterSuccess() {
        UUID customerId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        UUID restaurantId = UUID.randomUUID();
        UUID trackingId = UUID.randomUUID();

        GetOrdersQuery query = GetOrdersQuery.builder()
                .customerId(customerId)
                .page(0)
                .size(10)
                .filter(OrderFilterGroup.ACTIVE)
                .build();

        Order order = mock(Order.class);
        when(order.getId()).thenReturn(new OrderId(orderId));
        when(order.getTrackingId()).thenReturn(new com.berkay.order.service.domain.valueobject.TrackingId(trackingId));
        when(order.getRestaurantId()).thenReturn(new com.berkay.domain.valueobject.RestaurantId(restaurantId));
        when(order.getOrderStatus()).thenReturn(OrderStatus.PREPARING);
        when(order.getPrice()).thenReturn(new com.berkay.domain.valueobject.Money(new BigDecimal("80.00")));
        when(order.getItems()).thenReturn(List.of());

        OrderPageResult pageResult = new OrderPageResult(List.of(order), 0, 10, 1L, 1, true);
        when(orderRepository.findByCustomerIdAndOrderStatusIn(
                eq(new CustomerId(customerId)),
                eq(OrderFilterGroup.ACTIVE.getStatuses()),
                eq(0),
                eq(10))).thenReturn(pageResult);

        when(restaurantRepository.findRestaurantNamesByIds(List.of(restaurantId))).thenReturn(Map.of(restaurantId, "Pizza Hut"));

        com.berkay.order.service.domain.ports.output.repository.CustomerOrdersSummaryProjection summaryProjection =
                mock(com.berkay.order.service.domain.ports.output.repository.CustomerOrdersSummaryProjection.class);
        when(summaryProjection.getAllCount()).thenReturn(5L);
        when(summaryProjection.getActiveCount()).thenReturn(1L);
        when(summaryProjection.getSucceededCount()).thenReturn(3L);
        when(summaryProjection.getAbortCount()).thenReturn(1L);
        when(orderRepository.getCustomerOrdersSummary(new CustomerId(customerId))).thenReturn(summaryProjection);

        GetOrdersResponse response = handler.getOrders(query);

        assertNotNull(response);
        assertEquals(1, response.getOrders().size());
        assertEquals(1L, response.getTotalElements());
        assertEquals(1L, response.getActiveCount());
        assertEquals(5L, response.getAllCount());
        assertEquals("Pizza Hut", response.getOrders().get(0).getRestaurantName());

        verify(orderRepository).findByCustomerIdAndOrderStatusIn(any(), any(), eq(0), eq(10));
        verify(orderRepository, never()).findByCustomerId(any(), anyInt(), anyInt());
    }
}
