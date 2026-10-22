package com.berkay.order.service.application.rest;

import com.berkay.domain.valueobject.CancelReason;
import com.berkay.domain.valueobject.MaterialAction;
import com.berkay.domain.valueobject.OrderStatus;
import com.berkay.order.service.domain.dto.read.CustomerOrderDetailQuery;
import com.berkay.order.service.domain.dto.read.CustomerOrderDetailResponse;
import com.berkay.order.service.domain.dto.restaurant.DeliveryAddressResponse;
import com.berkay.order.service.domain.dto.restaurant.OrderDetailQuery;
import com.berkay.order.service.domain.dto.restaurant.RestaurantOrderItemResponse;
import com.berkay.order.service.domain.dto.restaurant.RestaurantOrderResponse;
import com.berkay.order.service.domain.dto.track.TrackOrderQuery;
import com.berkay.order.service.domain.dto.track.TrackOrderResponse;
import com.berkay.order.service.domain.ports.input.service.OrderApplicationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class OrderControllerTest {

    @Mock
    private OrderApplicationService orderApplicationService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        OrderController controller = new OrderController(orderApplicationService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    @DisplayName("GET /orders/{orderId} returns 200 OK with CustomerOrderDetailResponse and excludes internal fields")
    void testGetCustomerOrderDetail() throws Exception {
        UUID orderId = UUID.randomUUID();
        UUID trackingId = UUID.randomUUID();
        UUID restaurantId = UUID.randomUUID();

        CustomerOrderDetailResponse response = CustomerOrderDetailResponse.builder()
                .orderId(orderId)
                .trackingId(trackingId)
                .restaurantId(restaurantId)
                .orderStatus(OrderStatus.PREPARING)
                .price(new BigDecimal("200.00"))
                .deliveryFee(new BigDecimal("25.00"))
                .createdAt(ZonedDateTime.now())
                .items(List.of(
                        RestaurantOrderItemResponse.builder()
                                .productId(UUID.randomUUID())
                                .productName("Burger")
                                .quantity(2)
                                .price(new BigDecimal("100.00"))
                                .subTotal(new BigDecimal("200.00"))
                                .build()
                ))
                .deliveryAddress(DeliveryAddressResponse.builder()
                        .city("Istanbul")
                        .district("Kadikoy")
                        .street("Moda Cad.")
                        .build())
                .cancelReason(null)
                .build();

        when(orderApplicationService.getCustomerOrderDetail(any(CustomerOrderDetailQuery.class))).thenReturn(response);

        mockMvc.perform(get("/orders/{orderId}", orderId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(orderId.toString()))
                .andExpect(jsonPath("$.trackingId").value(trackingId.toString()))
                .andExpect(jsonPath("$.restaurantId").value(restaurantId.toString()))
                .andExpect(jsonPath("$.orderStatus").value("PREPARING"))
                .andExpect(jsonPath("$.price").value(200.00))
                .andExpect(jsonPath("$.deliveryFee").value(25.00))
                .andExpect(jsonPath("$.items[0].productName").value("Burger"))
                .andExpect(jsonPath("$.deliveryAddress.city").value("Istanbul"))
                .andExpect(jsonPath("$.materialAction").doesNotExist())
                .andExpect(jsonPath("$.internalNote").doesNotExist());

        verify(orderApplicationService).getCustomerOrderDetail(any(CustomerOrderDetailQuery.class));
    }

    @Test
    @DisplayName("GET /orders/{trackingId}/track returns 200 OK with TrackOrderResponse")
    void testGetOrderByTrackingId() throws Exception {
        UUID trackingId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();

        TrackOrderResponse response = TrackOrderResponse.builder()
                .orderTrackingId(trackingId)
                .customerId(customerId)
                .orderStatus(OrderStatus.IN_DELIVERY)
                .cancelReason(null)
                .internalNote(null)
                .build();

        when(orderApplicationService.trackOrder(any(TrackOrderQuery.class))).thenReturn(response);

        mockMvc.perform(get("/orders/{trackingId}/track", trackingId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderTrackingId").value(trackingId.toString()))
                .andExpect(jsonPath("$.customerId").value(customerId.toString()))
                .andExpect(jsonPath("$.orderStatus").value("IN_DELIVERY"));

        verify(orderApplicationService).trackOrder(any(TrackOrderQuery.class));
    }

    @Test
    @DisplayName("GET /orders/{orderId}/detail returns 200 OK with RestaurantOrderResponse including materialAction and internalNote")
    void testGetOrderDetailForRestaurant() throws Exception {
        UUID orderId = UUID.randomUUID();
        UUID trackingId = UUID.randomUUID();
        UUID restaurantId = UUID.randomUUID();

        RestaurantOrderResponse response = RestaurantOrderResponse.builder()
                .orderId(orderId)
                .trackingId(trackingId)
                .restaurantId(restaurantId)
                .orderStatus(OrderStatus.CANCELLED)
                .price(new BigDecimal("150.00"))
                .deliveryFee(new BigDecimal("20.00"))
                .createdAt(ZonedDateTime.now())
                .cancelReason(CancelReason.INSUFFICIENT_STOCK)
                .materialAction(MaterialAction.RESTORE_STOCK)
                .internalNote("Internal kitchen note")
                .build();

        when(orderApplicationService.getOrderDetail(any(OrderDetailQuery.class))).thenReturn(response);

        mockMvc.perform(get("/orders/{orderId}/detail", orderId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(orderId.toString()))
                .andExpect(jsonPath("$.orderStatus").value("CANCELLED"))
                .andExpect(jsonPath("$.cancelReason").value("INSUFFICIENT_STOCK"))
                .andExpect(jsonPath("$.materialAction").value("RESTORE_STOCK"))
                .andExpect(jsonPath("$.internalNote").value("Internal kitchen note"));

        verify(orderApplicationService).getOrderDetail(any(OrderDetailQuery.class));
    }

    @Test
    @DisplayName("GET /orders returns 200 OK with enriched OrderSummary list")
    void testGetOrders() throws Exception {
        UUID orderId = UUID.randomUUID();
        UUID trackingId = UUID.randomUUID();
        UUID restaurantId = UUID.randomUUID();

        com.berkay.order.service.domain.dto.read.OrderSummary summary = com.berkay.order.service.domain.dto.read.OrderSummary.builder()
                .orderId(orderId)
                .orderTrackingId(trackingId)
                .restaurantId(restaurantId)
                .restaurantName("Burger King")
                .orderStatus(OrderStatus.DELIVERED)
                .totalAmount(new BigDecimal("250.00"))
                .totalItemCount(3)
                .items(List.of(
                        com.berkay.order.service.domain.dto.read.OrderItemSummaryDto.builder().name("Whopper").quantity(2).build(),
                        com.berkay.order.service.domain.dto.read.OrderItemSummaryDto.builder().name("Kola").quantity(1).build()))
                .createdAt(ZonedDateTime.now())
                .build();

        com.berkay.order.service.domain.dto.read.GetOrdersResponse response = com.berkay.order.service.domain.dto.read.GetOrdersResponse.builder()
                .orders(List.of(summary))
                .page(0)
                .size(10)
                .totalElements(1)
                .totalPages(1)
                .isLast(true)
                .activeCount(2L)
                .succeededCount(12L)
                .abortCount(2L)
                .allCount(16L)
                .build();

        when(orderApplicationService.getOrders(any(com.berkay.order.service.domain.dto.read.GetOrdersQuery.class))).thenReturn(response);

        mockMvc.perform(get("/orders")
                        .param("filter", "ACTIVE")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orders[0].orderId").value(orderId.toString()))
                .andExpect(jsonPath("$.orders[0].restaurantName").value("Burger King"))
                .andExpect(jsonPath("$.orders[0].totalItemCount").value(3))
                .andExpect(jsonPath("$.orders[0].items[0].name").value("Whopper"))
                .andExpect(jsonPath("$.orders[0].items[0].quantity").value(2))
                .andExpect(jsonPath("$.orders[0].internalNote").doesNotExist())
                .andExpect(jsonPath("$.activeCount").value(2))
                .andExpect(jsonPath("$.succeededCount").value(12))
                .andExpect(jsonPath("$.abortCount").value(2))
                .andExpect(jsonPath("$.allCount").value(16));

        verify(orderApplicationService).getOrders(any(com.berkay.order.service.domain.dto.read.GetOrdersQuery.class));
    }
}
