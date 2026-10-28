package com.berkay.order.service.application.rest;

import com.berkay.order.service.domain.dto.review.*;
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

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class OrderReviewControllerTest {

    @Mock
    private OrderApplicationService orderApplicationService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        OrderReviewController controller = new OrderReviewController(orderApplicationService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    @DisplayName("POST /orders/{orderId}/reviews returns 201 Created on valid review creation")
    void testCreateReview() throws Exception {
        UUID orderId = UUID.randomUUID();
        UUID reviewId = UUID.randomUUID();

        CreateOrderReviewResponse response = CreateOrderReviewResponse.builder()
                .reviewId(reviewId)
                .message("Review created successfully")
                .build();

        when(orderApplicationService.createReview(any(CreateOrderReviewCommand.class))).thenReturn(response);

        String jsonPayload = """
                {
                    "rating": 5,
                    "comment": "Harika yemekler!"
                }
                """;

        mockMvc.perform(post("/orders/{orderId}/reviews", orderId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.reviewId").value(reviewId.toString()))
                .andExpect(jsonPath("$.message").value("Review created successfully"));

        verify(orderApplicationService).createReview(any(CreateOrderReviewCommand.class));
    }

    @Test
    @DisplayName("GET /orders/{orderId}/review returns 200 OK with customer review")
    void testGetCustomerOrderReview() throws Exception {
        UUID orderId = UUID.randomUUID();
        UUID reviewId = UUID.randomUUID();

        UUID restaurantId = UUID.randomUUID();

        CustomerOrderReviewResponse response = CustomerOrderReviewResponse.builder()
                .reviewId(reviewId)
                .orderId(orderId)
                .restaurantId(restaurantId)
                .rating(4)
                .comment("Hızlı teslimat, lezzetli.")
                .customerNameMasked("A*** Y***")
                .restaurantReply("Afiyet olsun!")
                .restaurantRepliedAt(ZonedDateTime.now())
                .createdAt(ZonedDateTime.now())
                .build();

        when(orderApplicationService.getCustomerOrderReview(eq(orderId))).thenReturn(response);

        mockMvc.perform(get("/orders/{orderId}/review", orderId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reviewId").value(reviewId.toString()))
                .andExpect(jsonPath("$.orderId").value(orderId.toString()))
                .andExpect(jsonPath("$.restaurantId").value(restaurantId.toString()))
                .andExpect(jsonPath("$.rating").value(4))
                .andExpect(jsonPath("$.comment").value("Hızlı teslimat, lezzetli."))
                .andExpect(jsonPath("$.customerNameMasked").value("A*** Y***"))
                .andExpect(jsonPath("$.restaurantReply").value("Afiyet olsun!"));

        verify(orderApplicationService).getCustomerOrderReview(eq(orderId));
    }

    @Test
    @DisplayName("POST /orders/restaurants/{restaurantId}/reviews/{reviewId}/reply returns 200 OK")
    void testReplyReview() throws Exception {
        UUID restaurantId = UUID.randomUUID();
        UUID reviewId = UUID.randomUUID();

        ReplyOrderReviewResponse response = ReplyOrderReviewResponse.builder()
                .reviewId(reviewId)
                .message("Review replied successfully")
                .build();

        when(orderApplicationService.replyReview(any(ReplyOrderReviewCommand.class))).thenReturn(response);

        String jsonPayload = """
                {
                    "reply": "Bizi tercih ettiğiniz için teşekkürler!"
                }
                """;

        mockMvc.perform(post("/orders/restaurants/{restaurantId}/reviews/{reviewId}/reply", restaurantId, reviewId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reviewId").value(reviewId.toString()))
                .andExpect(jsonPath("$.message").value("Review replied successfully"));

        verify(orderApplicationService).replyReview(any(ReplyOrderReviewCommand.class));
    }

    @Test
    @DisplayName("DELETE /orders/restaurants/{restaurantId}/reviews/{reviewId} returns 200 OK")
    void testDeactivateReview() throws Exception {
        UUID restaurantId = UUID.randomUUID();
        UUID reviewId = UUID.randomUUID();

        DeactivateOrderReviewResponse response = DeactivateOrderReviewResponse.builder()
                .reviewId(reviewId)
                .message("Review deactivated successfully")
                .build();

        when(orderApplicationService.deactivateReview(any(DeactivateOrderReviewCommand.class))).thenReturn(response);

        String jsonPayload = """
                {
                    "reason": "Uygunsuz içerik tespiti"
                }
                """;

        mockMvc.perform(delete("/orders/restaurants/{restaurantId}/reviews/{reviewId}", restaurantId, reviewId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reviewId").value(reviewId.toString()))
                .andExpect(jsonPath("$.message").value("Review deactivated successfully"));

        verify(orderApplicationService).deactivateReview(any(DeactivateOrderReviewCommand.class));
    }

    @Test
    @DisplayName("GET /orders/restaurants/{restaurantId}/reviews returns 200 OK with public review list")
    void testGetRestaurantReviews() throws Exception {
        UUID restaurantId = UUID.randomUUID();
        UUID reviewId = UUID.randomUUID();

        OrderReviewResponse reviewItem = OrderReviewResponse.builder()
                .reviewId(reviewId)
                .orderId(UUID.randomUUID())
                .restaurantId(restaurantId)
                .rating(5)
                .comment("Mükemmel")
                .customerNameMasked("A*** Y***")
                .restaurantReply("Teşekkürler")
                .restaurantRepliedAt(ZonedDateTime.now())
                .createdAt(ZonedDateTime.now())
                .canReply(false)
                .build();

        GetRestaurantReviewsResponse response = GetRestaurantReviewsResponse.builder()
                .reviews(List.of(reviewItem))
                .page(0)
                .size(10)
                .totalElements(1L)
                .totalPages(1)
                .isLast(true)
                .repliedCount(1L)
                .pendingReplyCount(0L)
                .build();

        when(orderApplicationService.getRestaurantReviews(any(GetRestaurantReviewsQuery.class))).thenReturn(response);

        mockMvc.perform(get("/orders/restaurants/{restaurantId}/reviews", restaurantId)
                        .param("status", "ALL")
                        .param("page", "0")
                        .param("size", "10")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.repliedCount").value(1))
                .andExpect(jsonPath("$.pendingReplyCount").value(0))
                .andExpect(jsonPath("$.reviews[0].reviewId").value(reviewId.toString()))
                .andExpect(jsonPath("$.reviews[0].restaurantId").value(restaurantId.toString()))
                .andExpect(jsonPath("$.reviews[0].rating").value(5))
                .andExpect(jsonPath("$.reviews[0].comment").value("Mükemmel"))
                .andExpect(jsonPath("$.reviews[0].customerNameMasked").value("A*** Y***"))
                .andExpect(jsonPath("$.reviews[0].canReply").value(false));

        verify(orderApplicationService).getRestaurantReviews(any(GetRestaurantReviewsQuery.class));
    }

    @Test
    @DisplayName("GET /orders/restaurants/{restaurantId}/reviews with status PENDING returns 200 OK")
    void testGetRestaurantReviewsPending() throws Exception {
        UUID restaurantId = UUID.randomUUID();
        UUID reviewId = UUID.randomUUID();

        OrderReviewResponse reviewItem = OrderReviewResponse.builder()
                .reviewId(reviewId)
                .orderId(UUID.randomUUID())
                .restaurantId(restaurantId)
                .rating(3)
                .comment("Yanıt bekliyor")
                .customerNameMasked("B*** K***")
                .createdAt(ZonedDateTime.now())
                .canReply(true)
                .build();

        GetRestaurantReviewsResponse response = GetRestaurantReviewsResponse.builder()
                .reviews(List.of(reviewItem))
                .page(0)
                .size(10)
                .totalElements(1L)
                .totalPages(1)
                .isLast(true)
                .repliedCount(5L)
                .pendingReplyCount(1L)
                .build();

        when(orderApplicationService.getRestaurantReviews(any(GetRestaurantReviewsQuery.class))).thenReturn(response);

        mockMvc.perform(get("/orders/restaurants/{restaurantId}/reviews", restaurantId)
                        .param("status", "PENDING")
                        .param("page", "0")
                        .param("size", "10")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.repliedCount").value(5))
                .andExpect(jsonPath("$.pendingReplyCount").value(1))
                .andExpect(jsonPath("$.reviews[0].canReply").value(true));
    }

    @Test
    @DisplayName("GET /orders/admin/restaurants/{restaurantId}/reviews returns 200 OK with admin review list")
    void testGetAdminRestaurantReviews() throws Exception {
        UUID restaurantId = UUID.randomUUID();
        UUID reviewId = UUID.randomUUID();

        AdminOrderReviewResponse item = AdminOrderReviewResponse.builder()
                .reviewId(reviewId)
                .orderId(UUID.randomUUID())
                .customerId(UUID.randomUUID())
                .restaurantId(restaurantId)
                .rating(1)
                .comment("Kötü deneyim")
                .customerNameMasked("M*** Y***")
                .restaurantReply(null)
                .isActive(false)
                .deactivationReason("Hakaret")
                .deactivatedAt(ZonedDateTime.now())
                .deactivatedBy(UUID.randomUUID())
                .createdAt(ZonedDateTime.now())
                .updatedAt(ZonedDateTime.now())
                .build();

        GetAdminRestaurantReviewsResponse response = GetAdminRestaurantReviewsResponse.builder()
                .reviews(List.of(item))
                .page(0)
                .size(10)
                .totalElements(1L)
                .totalPages(1)
                .isLast(true)
                .build();

        when(orderApplicationService.getAdminRestaurantReviews(any(GetAdminRestaurantReviewsQuery.class))).thenReturn(response);

        mockMvc.perform(get("/orders/admin/restaurants/{restaurantId}/reviews", restaurantId)
                        .param("status", "INACTIVE")
                        .param("page", "0")
                        .param("size", "10")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.reviews[0].reviewId").value(reviewId.toString()))
                .andExpect(jsonPath("$.reviews[0].customerNameMasked").value("M*** Y***"))
                .andExpect(jsonPath("$.reviews[0].active").value(false))
                .andExpect(jsonPath("$.reviews[0].deactivationReason").value("Hakaret"));

        verify(orderApplicationService).getAdminRestaurantReviews(any(GetAdminRestaurantReviewsQuery.class));
    }
}
