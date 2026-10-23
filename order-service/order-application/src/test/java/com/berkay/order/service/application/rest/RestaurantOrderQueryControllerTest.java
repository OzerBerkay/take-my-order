package com.berkay.order.service.application.rest;

import com.berkay.order.service.domain.dto.restaurant.RestaurantOrdersSummaryResponse;
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
import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class RestaurantOrderQueryControllerTest {

    @Mock
    private OrderApplicationService orderApplicationService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        RestaurantOrderQueryController controller = new RestaurantOrderQueryController(orderApplicationService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    @DisplayName("GET /orders/restaurants/{restaurantId}/summary returns 200 OK with correct summary payload")
    void testGetRestaurantOrdersSummary() throws Exception {
        UUID restaurantId = UUID.randomUUID();
        RestaurantOrdersSummaryResponse response = RestaurantOrdersSummaryResponse.builder()
                .totalCount(10)
                .waitingCount(2)
                .approvedCount(1)
                .preparingCount(3)
                .readyCount(1)
                .inDeliveryCount(1)
                .deliveredCount(1)
                .cancelledCount(1)
                .build();

        when(orderApplicationService.getRestaurantOrdersSummary(restaurantId)).thenReturn(response);

        mockMvc.perform(get("/orders/restaurants/{restaurantId}/summary", restaurantId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCount").value(10))
                .andExpect(jsonPath("$.waitingCount").value(2))
                .andExpect(jsonPath("$.approvedCount").value(1))
                .andExpect(jsonPath("$.preparingCount").value(3))
                .andExpect(jsonPath("$.readyCount").value(1))
                .andExpect(jsonPath("$.inDeliveryCount").value(1))
                .andExpect(jsonPath("$.deliveredCount").value(1))
                .andExpect(jsonPath("$.cancelledCount").value(1));

        verify(orderApplicationService).getRestaurantOrdersSummary(restaurantId);
    }
}
