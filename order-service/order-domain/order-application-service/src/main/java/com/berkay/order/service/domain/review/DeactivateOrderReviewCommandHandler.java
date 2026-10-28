package com.berkay.order.service.domain.review;

import com.berkay.order.service.domain.dto.restaurant.RestaurantRatingCalculationResult;
import com.berkay.order.service.domain.dto.review.DeactivateOrderReviewCommand;
import com.berkay.order.service.domain.dto.review.DeactivateOrderReviewResponse;
import com.berkay.order.service.domain.entity.OrderReview;
import com.berkay.order.service.domain.exception.OrderDomainException;
import com.berkay.order.service.domain.exception.ReviewNotFoundException;
import com.berkay.order.service.domain.mapper.OrderReviewDataMapper;
import com.berkay.order.service.domain.outbox.model.rating.RestaurantRatingEventPayload;
import com.berkay.order.service.domain.outbox.scheduler.rating.RestaurantRatingOutboxHelper;
import com.berkay.order.service.domain.ports.output.repository.OrderReviewRepository;
import com.berkay.order.service.domain.ports.output.repository.RestaurantRepository;
import com.berkay.order.service.domain.valueobject.OrderReviewId;
import com.berkay.outbox.OutboxStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.UUID;

@Slf4j
@Component
public class DeactivateOrderReviewCommandHandler {

    private final OrderReviewRepository orderReviewRepository;
    private final RestaurantRepository restaurantRepository;
    private final OrderReviewDataMapper orderReviewDataMapper;
    private final RestaurantRatingOutboxHelper ratingOutboxHelper;

    public DeactivateOrderReviewCommandHandler(OrderReviewRepository orderReviewRepository,
                                               RestaurantRepository restaurantRepository,
                                               OrderReviewDataMapper orderReviewDataMapper,
                                               RestaurantRatingOutboxHelper ratingOutboxHelper) {
        this.orderReviewRepository = orderReviewRepository;
        this.restaurantRepository = restaurantRepository;
        this.orderReviewDataMapper = orderReviewDataMapper;
        this.ratingOutboxHelper = ratingOutboxHelper;
    }

    @Transactional
    public DeactivateOrderReviewResponse deactivateReview(DeactivateOrderReviewCommand command) {
        log.info("Deactivating review {} for restaurant {}", command.getReviewId(), command.getRestaurantId());

        OrderReview review = orderReviewRepository.findById(new OrderReviewId(command.getReviewId()))
                .orElseThrow(() -> new ReviewNotFoundException("Review not found with id: " + command.getReviewId()));

        if (!review.getRestaurantId().getValue().equals(command.getRestaurantId())) {
            log.warn("Review {} does not belong to restaurant {}", command.getReviewId(), command.getRestaurantId());
            throw new OrderDomainException("Review does not belong to restaurant: " + command.getRestaurantId());
        }

        // Pessimistic lock on restaurant row
        restaurantRepository.findRestaurantByIdWithLock(command.getRestaurantId())
                .orElseThrow(() -> new OrderDomainException("Restaurant not found with id: " + command.getRestaurantId()));

        review.deactivate(command.getReason(), command.getModeratorId());
        OrderReview savedReview = orderReviewRepository.save(review);

        // Recalculate rating & outbox
        RestaurantRatingCalculationResult calcResult = orderReviewRepository.calculateRatingAndCount(command.getRestaurantId());
        RestaurantRatingEventPayload payload = RestaurantRatingEventPayload.builder()
                .id(UUID.randomUUID().toString())
                .restaurantId(command.getRestaurantId().toString())
                .averageRating(calcResult.getAverageRating())
                .totalReviews((int) calcResult.getTotalReviews())
                .updatedAt(ZonedDateTime.now(ZoneId.of("UTC")))
                .build();

        ratingOutboxHelper.saveRatingOutboxMessage(payload, OutboxStatus.STARTED);

        log.info("Review {} successfully deactivated for restaurant {}", command.getReviewId(), command.getRestaurantId());
        return orderReviewDataMapper.orderReviewToDeactivateOrderReviewResponse(savedReview, "Yorum başarıyla deaktif edildi ve yayından kaldırıldı.");
    }
}
