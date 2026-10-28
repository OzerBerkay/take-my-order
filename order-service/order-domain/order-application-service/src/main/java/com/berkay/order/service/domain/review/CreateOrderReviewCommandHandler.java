package com.berkay.order.service.domain.review;

import com.berkay.domain.valueobject.OrderId;
import com.berkay.domain.valueobject.OrderStatus;
import com.berkay.order.service.domain.dto.restaurant.RestaurantRatingCalculationResult;
import com.berkay.order.service.domain.dto.review.CreateOrderReviewCommand;
import com.berkay.order.service.domain.dto.review.CreateOrderReviewResponse;
import com.berkay.order.service.domain.entity.Customer;
import com.berkay.order.service.domain.entity.Order;
import com.berkay.order.service.domain.entity.OrderReview;
import com.berkay.order.service.domain.entity.Restaurant;
import com.berkay.order.service.domain.exception.*;
import com.berkay.order.service.domain.mapper.OrderReviewDataMapper;
import com.berkay.order.service.domain.outbox.model.rating.RestaurantRatingEventPayload;
import com.berkay.order.service.domain.outbox.scheduler.rating.RestaurantRatingOutboxHelper;
import com.berkay.order.service.domain.ports.output.repository.CustomerRepository;
import com.berkay.order.service.domain.ports.output.repository.OrderRepository;
import com.berkay.order.service.domain.ports.output.repository.OrderReviewRepository;
import com.berkay.order.service.domain.ports.output.repository.RestaurantRepository;
import com.berkay.outbox.OutboxStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.UUID;

@Slf4j
@Component
public class CreateOrderReviewCommandHandler {

    private final OrderRepository orderRepository;
    private final RestaurantRepository restaurantRepository;
    private final CustomerRepository customerRepository;
    private final OrderReviewRepository orderReviewRepository;
    private final OrderReviewDataMapper orderReviewDataMapper;
    private final RestaurantRatingOutboxHelper ratingOutboxHelper;

    public CreateOrderReviewCommandHandler(OrderRepository orderRepository,
                                           RestaurantRepository restaurantRepository,
                                           CustomerRepository customerRepository,
                                           OrderReviewRepository orderReviewRepository,
                                           OrderReviewDataMapper orderReviewDataMapper,
                                           RestaurantRatingOutboxHelper ratingOutboxHelper) {
        this.orderRepository = orderRepository;
        this.restaurantRepository = restaurantRepository;
        this.customerRepository = customerRepository;
        this.orderReviewRepository = orderReviewRepository;
        this.orderReviewDataMapper = orderReviewDataMapper;
        this.ratingOutboxHelper = ratingOutboxHelper;
    }

    @Transactional
    public CreateOrderReviewResponse createReview(CreateOrderReviewCommand command) {
        log.info("Creating review for order id: {}", command.getOrderId());

        Order order = orderRepository.findById(new OrderId(command.getOrderId()))
                .orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + command.getOrderId()));

        if (order.getOrderStatus() != OrderStatus.DELIVERED) {
            log.warn("Order {} is not delivered yet! Current status: {}", command.getOrderId(), order.getOrderStatus());
            throw new OrderNotDeliveredException("Order must be delivered before it can be reviewed! Current status: " + order.getOrderStatus());
        }

        ZonedDateTime deliveredAt = order.getDeliveredAt() != null ? order.getDeliveredAt() : order.getCreatedAt();
        if (deliveredAt.plusDays(7).isBefore(ZonedDateTime.now(ZoneId.of("UTC")))) {
            log.warn("Review window of 7 days expired for order id: {}", command.getOrderId());
            throw new ReviewTimeExpiredException("The 7-day window to review this order has expired!");
        }

        if (order.isReviewed() || orderReviewRepository.findByOrderId(command.getOrderId()).isPresent()) {
            log.warn("Order {} has already been reviewed!", command.getOrderId());
            throw new ReviewAlreadyExistsException("This order has already been reviewed!");
        }

        // Pessimistic lock on restaurant row
        UUID restaurantId = order.getRestaurantId().getValue();
        Restaurant restaurant = restaurantRepository.findRestaurantByIdWithLock(restaurantId)
                .orElseThrow(() -> new OrderDomainException("Restaurant not found with id: " + restaurantId));

        if (!restaurant.isActive()) {
            log.warn("Cannot review inactive restaurant: {}", restaurantId);
            throw new OrderDomainException("Cannot review an inactive or closed restaurant!");
        }

        Customer customer = customerRepository.findCustomer(order.getCustomerId().getValue()).orElse(null);

        OrderReview orderReview = orderReviewDataMapper.createOrderReviewCommandToOrderReview(command, order, customer);
        orderReview.initializeReview();
        OrderReview savedReview = orderReviewRepository.save(orderReview);

        order.markAsReviewed();
        orderRepository.save(order);

        // Recalculate rating & outbox
        RestaurantRatingCalculationResult calcResult = orderReviewRepository.calculateRatingAndCount(restaurantId);
        RestaurantRatingEventPayload payload = RestaurantRatingEventPayload.builder()
                .id(UUID.randomUUID().toString())
                .restaurantId(restaurantId.toString())
                .averageRating(calcResult.getAverageRating())
                .totalReviews((int) calcResult.getTotalReviews())
                .updatedAt(ZonedDateTime.now(ZoneId.of("UTC")))
                .build();

        ratingOutboxHelper.saveRatingOutboxMessage(payload, OutboxStatus.STARTED);

        log.info("Order review successfully created with id: {} for order: {}", savedReview.getId().getValue(), command.getOrderId());
        return orderReviewDataMapper.orderReviewToCreateOrderReviewResponse(savedReview, "Değerlendirmeniz başarıyla kaydedildi.");
    }
}
