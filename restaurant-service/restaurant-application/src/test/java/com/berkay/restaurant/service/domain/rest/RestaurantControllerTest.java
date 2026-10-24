package com.berkay.restaurant.service.domain.rest;

import com.berkay.restaurant.service.domain.dto.create.restaurant.CreateRestaurantCommand;
import com.berkay.restaurant.service.domain.dto.create.restaurant.CreateRestaurantResponse;
import com.berkay.restaurant.service.domain.dto.read.GetRestaurantQuery;
import com.berkay.restaurant.service.domain.dto.read.GetRestaurantQueryResponse;
import com.berkay.restaurant.service.domain.dto.update.UpdateRestaurantRequest;
import com.berkay.restaurant.service.domain.dto.update.restaurant.UpdateRestaurantCommand;
import com.berkay.restaurant.service.domain.mapper.ProductRequestMapper;
import com.berkay.restaurant.service.domain.mapper.RestaurantRequestMapper;
import com.berkay.restaurant.service.domain.ports.input.service.RestaurantApplicationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RestaurantControllerTest {

    @Mock
    private RestaurantApplicationService restaurantApplicationService;

    @Mock
    private RestaurantRequestMapper restaurantRequestMapper;

    @Mock
    private ProductRequestMapper productRequestMapper;

    @InjectMocks
    private RestaurantController restaurantController;

    private UUID restaurantId;
    private UUID merchantId;

    @BeforeEach
    void setUp() {
        restaurantId = UUID.randomUUID();
        merchantId = UUID.randomUUID();
    }

    @Test
    void shouldCreateRestaurantWithHoursSuccessfully() {
        LocalTime opening = LocalTime.of(9, 0);
        LocalTime closing = LocalTime.of(22, 0);

        CreateRestaurantCommand command = CreateRestaurantCommand.builder()
                .restaurantName("Test Bistro")
                .openingTime(opening)
                .closingTime(closing)
                .build();

        CreateRestaurantResponse expectedResponse = CreateRestaurantResponse.builder()
                .restaurantId(restaurantId)
                .message("Restaurant created successfully")
                .build();

        when(restaurantApplicationService.createRestaurant(command)).thenReturn(expectedResponse);

        ResponseEntity<CreateRestaurantResponse> response = restaurantController.createRestaurant(command, merchantId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(restaurantId, response.getBody().getRestaurantId());
        assertEquals(merchantId.toString(), command.getMerchantId());
        verify(restaurantApplicationService, times(1)).createRestaurant(command);
    }

    @Test
    void shouldUpdateRestaurantWithHoursSuccessfully() {
        LocalTime opening = LocalTime.of(10, 0);
        LocalTime closing = LocalTime.of(23, 0);

        UpdateRestaurantRequest request = UpdateRestaurantRequest.builder()
                .restaurantName("Updated Bistro")
                .openingTime(opening)
                .closingTime(closing)
                .build();

        UpdateRestaurantCommand command = UpdateRestaurantCommand.builder()
                .restaurantId(restaurantId)
                .restaurantName("Updated Bistro")
                .openingTime(opening)
                .closingTime(closing)
                .build();

        when(restaurantRequestMapper.updateRestaurantRequestToCommand(restaurantId, request)).thenReturn(command);
        doNothing().when(restaurantApplicationService).updateRestaurant(command);

        ResponseEntity<String> response = restaurantController.updateRestaurant(restaurantId, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Restaurant updated", response.getBody());
        verify(restaurantRequestMapper, times(1)).updateRestaurantRequestToCommand(restaurantId, request);
        verify(restaurantApplicationService, times(1)).updateRestaurant(command);
    }

    @Test
    void shouldGetRestaurantWithHoursSuccessfully() {
        LocalTime opening = LocalTime.of(8, 0);
        LocalTime closing = LocalTime.of(21, 0);

        GetRestaurantQueryResponse expectedResponse = GetRestaurantQueryResponse.builder()
                .restaurantId(restaurantId)
                .name("Test Bistro")
                .openingTime(opening)
                .closingTime(closing)
                .build();

        when(restaurantApplicationService.getRestaurant(any(GetRestaurantQuery.class))).thenReturn(expectedResponse);

        ResponseEntity<GetRestaurantQueryResponse> response = restaurantController.getRestaurant(restaurantId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(opening, response.getBody().getOpeningTime());
        assertEquals(closing, response.getBody().getClosingTime());
        verify(restaurantApplicationService, times(1)).getRestaurant(any(GetRestaurantQuery.class));
    }

    @Test
    void shouldReturn400ForInvalidTimeFormats_WhenPatchingRestaurant() throws Exception {
        org.springframework.test.web.servlet.MockMvc mockMvc = org.springframework.test.web.servlet.setup.MockMvcBuilders
                .standaloneSetup(restaurantController)
                .setControllerAdvice(new com.berkay.restaurant.service.domain.exception.handler.RestaurantGlobalExceptionHandler())
                .build();

        String[] invalidTimes = {"0024", "26:30", "001:15", "00:155", "12:65", "abc", "-12:-10", "10:5", "9:15", "9:1"};

        for (String invalidTime : invalidTimes) {
            String jsonPayload = String.format("{\"openingTime\": \"%s\"}", invalidTime);

            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                            .patch("/restaurants/" + restaurantId)
                            .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                            .content(jsonPayload))
                    .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isBadRequest())
                    .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.code").value("Bad Request"))
                    .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.message")
                            .value("Invalid time format for field 'openingTime'. Expected format is 'HH:mm' (e.g. '09:00', '22:30')."));
        }
    }

    @Test
    void shouldReturn400ForInvalidClosingTimeFormat_WhenPatchingRestaurant() throws Exception {
        org.springframework.test.web.servlet.MockMvc mockMvc = org.springframework.test.web.servlet.setup.MockMvcBuilders
                .standaloneSetup(restaurantController)
                .setControllerAdvice(new com.berkay.restaurant.service.domain.exception.handler.RestaurantGlobalExceptionHandler())
                .build();

        String jsonPayload = "{\"closingTime\": \"25:00\"}";

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .patch("/restaurants/" + restaurantId)
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isBadRequest())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.code").value("Bad Request"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.message")
                        .value("Invalid time format for field 'closingTime'. Expected format is 'HH:mm' (e.g. '09:00', '22:30')."));
    }

    @Test
    void shouldReturn400ForInvalidTimeFormat_WhenCreatingRestaurant() throws Exception {
        org.springframework.test.web.servlet.MockMvc mockMvc = org.springframework.test.web.servlet.setup.MockMvcBuilders
                .standaloneSetup(restaurantController)
                .setControllerAdvice(new com.berkay.restaurant.service.domain.exception.handler.RestaurantGlobalExceptionHandler())
                .build();

        String jsonPayload = "{\"restaurantName\": \"Gourmet\", \"city\": \"Istanbul\", \"openingTime\": \"0024\"}";

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .post("/restaurants")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isBadRequest())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.code").value("Bad Request"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.message")
                        .value("Invalid time format for field 'openingTime'. Expected format is 'HH:mm' (e.g. '09:00', '22:30')."));
    }
}
