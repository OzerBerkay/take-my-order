package com.berkay.order.service.domain.review;

import com.berkay.domain.valueobject.CustomerId;
import com.berkay.domain.valueobject.OrderId;
import com.berkay.domain.valueobject.OrderStatus;
import com.berkay.domain.valueobject.RestaurantId;
import com.berkay.order.service.domain.dto.restaurant.RestaurantRatingCalculationResult;
import com.berkay.order.service.domain.dto.review.CreateOrderReviewCommand;
import com.berkay.order.service.domain.dto.review.CreateOrderReviewResponse;
import com.berkay.order.service.domain.entity.Customer;
import com.berkay.order.service.domain.entity.Order;
import com.berkay.order.service.domain.entity.OrderReview;
import com.berkay.order.service.domain.entity.Restaurant;
import com.berkay.order.service.domain.exception.OrderNotDeliveredException;
import com.berkay.order.service.domain.exception.OrderNotFoundException;
import com.berkay.order.service.domain.exception.ReviewAlreadyExistsException;
import com.berkay.order.service.domain.exception.ReviewTimeExpiredException;
import com.berkay.order.service.domain.mapper.OrderReviewDataMapper;
import com.berkay.order.service.domain.outbox.scheduler.rating.RestaurantRatingOutboxHelper;
import com.berkay.order.service.domain.ports.output.repository.CustomerRepository;
import com.berkay.order.service.domain.ports.output.repository.OrderRepository;
import com.berkay.order.service.domain.ports.output.repository.OrderReviewRepository;
import com.berkay.order.service.domain.ports.output.repository.RestaurantRepository;
import com.berkay.order.service.domain.valueobject.OrderReviewId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateOrderReviewCommandHandlerTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private RestaurantRepository restaurantRepository;
    @Mock
    private CustomerRepository customerRepository;
    @Mock
    private OrderReviewRepository orderReviewRepository;
    @Mock
    private OrderReviewDataMapper orderReviewDataMapper;
    @Mock
    private RestaurantRatingOutboxHelper ratingOutboxHelper;

    private CreateOrderReviewCommandHandler handler;

    @BeforeEach
    void setUp() {
        handler = new CreateOrderReviewCommandHandler(
                orderRepository,
                restaurantRepository,
                customerRepository,
                orderReviewRepository,
                orderReviewDataMapper,
                ratingOutboxHelper
        );
    }

    @Test
    @DisplayName("Should create review successfully for delivered order within 7 days")
    void testCreateReviewSuccess() {
        UUID orderId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        UUID restaurantId = UUID.randomUUID();
        UUID reviewId = UUID.randomUUID();

        CreateOrderReviewCommand command = CreateOrderReviewCommand.builder()
                .orderId(orderId)
                .rating(5)
                .comment("Mukemmel servis!")
                .build();

        Order order = mock(Order.class);
        when(order.getOrderStatus()).thenReturn(OrderStatus.DELIVERED);
        when(order.getDeliveredAt()).thenReturn(ZonedDateTime.now(ZoneId.of("UTC")).minusDays(1));
        when(order.isReviewed()).thenReturn(false);
        when(order.getRestaurantId()).thenReturn(new RestaurantId(restaurantId));
        when(order.getCustomerId()).thenReturn(new CustomerId(customerId));

        when(orderRepository.findById(new OrderId(orderId))).thenReturn(Optional.of(order));
        Restaurant restaurant = mock(Restaurant.class);
        when(restaurant.isActive()).thenReturn(true);
        when(restaurantRepository.findRestaurantByIdWithLock(restaurantId)).thenReturn(Optional.of(restaurant));

        Customer customer = new Customer(new CustomerId(customerId), "bozer", "Berkay", "Özer", "bozer@test.com");
        when(customerRepository.findCustomer(customerId)).thenReturn(Optional.of(customer));

        OrderReview initializedReview = OrderReview.builder()
                .id(new OrderReviewId(reviewId))
                .orderId(new OrderId(orderId))
                .customerId(new CustomerId(customerId))
                .restaurantId(new RestaurantId(restaurantId))
                .rating(5)
                .comment("Mukemmel servis!")
                .customerNameMasked("B*** Ö***")
                .build();
        when(orderReviewDataMapper.createOrderReviewCommandToOrderReview(command, order, customer))
                .thenReturn(initializedReview);

        OrderReview savedReview = mock(OrderReview.class);
        when(savedReview.getId()).thenReturn(new OrderReviewId(reviewId));
        when(orderReviewRepository.save(any(OrderReview.class))).thenReturn(savedReview);

        RestaurantRatingCalculationResult calcResult = new RestaurantRatingCalculationResult(10L, 4.8);
        when(orderReviewRepository.calculateRatingAndCount(restaurantId)).thenReturn(calcResult);

        CreateOrderReviewResponse expectedResponse = CreateOrderReviewResponse.builder()
                .reviewId(reviewId)
                .message("Değerlendirmeniz başarıyla kaydedildi.")
                .build();
        when(orderReviewDataMapper.orderReviewToCreateOrderReviewResponse(eq(savedReview), anyString()))
                .thenReturn(expectedResponse);

        CreateOrderReviewResponse response = handler.createReview(command);

        assertNotNull(response);
        assertEquals(reviewId, response.getReviewId());

        verify(order).markAsReviewed();
        verify(orderRepository).save(order);
        verify(orderReviewRepository).save(any(OrderReview.class));
        verify(ratingOutboxHelper).saveRatingOutboxMessage(any(), any());
    }

    @Test
    @DisplayName("Should throw OrderNotFoundException when order does not exist")
    void testCreateReviewOrderNotFound() {
        UUID orderId = UUID.randomUUID();
        CreateOrderReviewCommand command = CreateOrderReviewCommand.builder()
                .orderId(orderId)
                .rating(4)
                .build();

        when(orderRepository.findById(new OrderId(orderId))).thenReturn(Optional.empty());

        assertThrows(OrderNotFoundException.class, () -> handler.createReview(command));
    }

    @Test
    @DisplayName("Should throw OrderNotDeliveredException when order is not DELIVERED")
    void testCreateReviewOrderNotDelivered() {
        UUID orderId = UUID.randomUUID();
        CreateOrderReviewCommand command = CreateOrderReviewCommand.builder()
                .orderId(orderId)
                .rating(4)
                .build();

        Order order = mock(Order.class);
        when(order.getOrderStatus()).thenReturn(OrderStatus.APPROVED);
        when(orderRepository.findById(new OrderId(orderId))).thenReturn(Optional.of(order));

        assertThrows(OrderNotDeliveredException.class, () -> handler.createReview(command));
    }

    @Test
    @DisplayName("Should throw ReviewTimeExpiredException when order delivered more than 7 days ago")
    void testCreateReviewExpiredSevenDays() {
        UUID orderId = UUID.randomUUID();
        CreateOrderReviewCommand command = CreateOrderReviewCommand.builder()
                .orderId(orderId)
                .rating(4)
                .build();

        Order order = mock(Order.class);
        when(order.getOrderStatus()).thenReturn(OrderStatus.DELIVERED);
        when(order.getDeliveredAt()).thenReturn(ZonedDateTime.now(ZoneId.of("UTC")).minusDays(8));
        when(orderRepository.findById(new OrderId(orderId))).thenReturn(Optional.of(order));

        assertThrows(ReviewTimeExpiredException.class, () -> handler.createReview(command));
    }

    @Test
    @DisplayName("Should throw ReviewAlreadyExistsException when order was already reviewed")
    void testCreateReviewAlreadyReviewed() {
        UUID orderId = UUID.randomUUID();
        CreateOrderReviewCommand command = CreateOrderReviewCommand.builder()
                .orderId(orderId)
                .rating(4)
                .build();

        Order order = mock(Order.class);
        when(order.getOrderStatus()).thenReturn(OrderStatus.DELIVERED);
        when(order.getDeliveredAt()).thenReturn(ZonedDateTime.now(ZoneId.of("UTC")).minusDays(1));
        when(order.isReviewed()).thenReturn(true);
        when(orderRepository.findById(new OrderId(orderId))).thenReturn(Optional.of(order));

        assertThrows(ReviewAlreadyExistsException.class, () -> handler.createReview(command));
    }
}
