package com.berkay.order.service.domain;

import com.berkay.domain.valueobject.OrderStatus;
import com.berkay.order.service.domain.dto.restaurant.PlatformOrderQuery;
import com.berkay.order.service.domain.dto.restaurant.RestaurantOrderResponse;
import com.berkay.order.service.domain.entity.Order;
import com.berkay.order.service.domain.exception.OrderNotFoundException;
import com.berkay.order.service.domain.mapper.OrderDataMapper;
import com.berkay.order.service.domain.ports.output.repository.OrderRepository;
import com.berkay.order.service.domain.valueobject.TrackingId;
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
class PlatformOrderQueryHandlerTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private OrderDataMapper orderDataMapper;

    private PlatformOrderQueryHandler handler;

    @BeforeEach
    void setUp() {
        handler = new PlatformOrderQueryHandler(orderRepository, orderDataMapper);
    }

    @Test
    @DisplayName("Successfully returns platform order by trackingId")
    void testGetPlatformOrderSuccess() {
        UUID trackingId = UUID.randomUUID();
        PlatformOrderQuery query = PlatformOrderQuery.builder().trackingId(trackingId).build();
        Order order = mock(Order.class);

        when(orderRepository.findByTrackingId(new TrackingId(trackingId))).thenReturn(Optional.of(order));

        RestaurantOrderResponse response = RestaurantOrderResponse.builder()
                .trackingId(trackingId)
                .orderStatus(OrderStatus.IN_DELIVERY)
                .build();
        when(orderDataMapper.orderToRestaurantOrderResponse(order)).thenReturn(response);

        RestaurantOrderResponse result = handler.getPlatformOrder(query);

        assertNotNull(result);
        assertEquals(trackingId, result.getTrackingId());
        assertEquals(OrderStatus.IN_DELIVERY, result.getOrderStatus());
    }

    @Test
    @DisplayName("Throws OrderNotFoundException when trackingId not found")
    void testGetPlatformOrderNotFound() {
        UUID trackingId = UUID.randomUUID();
        PlatformOrderQuery query = PlatformOrderQuery.builder().trackingId(trackingId).build();

        when(orderRepository.findByTrackingId(new TrackingId(trackingId))).thenReturn(Optional.empty());

        assertThrows(OrderNotFoundException.class, () -> handler.getPlatformOrder(query));
    }
}
