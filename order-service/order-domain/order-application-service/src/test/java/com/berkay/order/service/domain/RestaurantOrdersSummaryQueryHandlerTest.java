package com.berkay.order.service.domain;

import com.berkay.domain.valueobject.RestaurantId;
import com.berkay.order.service.domain.dto.restaurant.RestaurantOrdersSummaryResponse;
import com.berkay.order.service.domain.ports.output.repository.OrderRepository;
import com.berkay.order.service.domain.ports.output.repository.RestaurantOrdersSummaryProjection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RestaurantOrdersSummaryQueryHandlerTest {

    @Mock
    private OrderRepository orderRepository;

    private RestaurantOrdersSummaryQueryHandler handler;

    @BeforeEach
    void setUp() {
        handler = new RestaurantOrdersSummaryQueryHandler(orderRepository);
    }

    @Test
    @DisplayName("Successfully returns mapped RestaurantOrdersSummaryResponse when projection exists")
    void testGetRestaurantOrdersSummarySuccess() {
        UUID restaurantId = UUID.randomUUID();
        RestaurantOrdersSummaryProjection projection = new RestaurantOrdersSummaryProjection() {
            @Override
            public long getTotalCount() {
                return 15L;
            }

            @Override
            public long getWaitingCount() {
                return 3L;
            }

            @Override
            public long getApprovedCount() {
                return 2L;
            }

            @Override
            public long getPreparingCount() {
                return 4L;
            }

            @Override
            public long getReadyCount() {
                return 1L;
            }

            @Override
            public long getInDeliveryCount() {
                return 2L;
            }

            @Override
            public long getDeliveredCount() {
                return 2L;
            }

            @Override
            public long getCancelledCount() {
                return 1L;
            }
        };

        when(orderRepository.getRestaurantOrdersSummary(eq(new RestaurantId(restaurantId))))
                .thenReturn(projection);

        RestaurantOrdersSummaryResponse response = handler.getRestaurantOrdersSummary(restaurantId);

        assertNotNull(response);
        assertEquals(15L, response.getTotalCount());
        assertEquals(3L, response.getWaitingCount());
        assertEquals(2L, response.getApprovedCount());
        assertEquals(4L, response.getPreparingCount());
        assertEquals(1L, response.getReadyCount());
        assertEquals(2L, response.getInDeliveryCount());
        assertEquals(2L, response.getDeliveredCount());
        assertEquals(1L, response.getCancelledCount());
    }

    @Test
    @DisplayName("Returns zero values when projection is null")
    void testGetRestaurantOrdersSummaryWhenProjectionIsNull() {
        UUID restaurantId = UUID.randomUUID();

        when(orderRepository.getRestaurantOrdersSummary(eq(new RestaurantId(restaurantId))))
                .thenReturn(null);

        RestaurantOrdersSummaryResponse response = handler.getRestaurantOrdersSummary(restaurantId);

        assertNotNull(response);
        assertEquals(0L, response.getTotalCount());
        assertEquals(0L, response.getWaitingCount());
        assertEquals(0L, response.getApprovedCount());
        assertEquals(0L, response.getPreparingCount());
        assertEquals(0L, response.getReadyCount());
        assertEquals(0L, response.getInDeliveryCount());
        assertEquals(0L, response.getDeliveredCount());
        assertEquals(0L, response.getCancelledCount());
    }
}
