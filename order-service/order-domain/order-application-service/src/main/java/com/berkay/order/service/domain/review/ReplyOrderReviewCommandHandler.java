package com.berkay.order.service.domain.review;

import com.berkay.order.service.domain.dto.review.ReplyOrderReviewCommand;
import com.berkay.order.service.domain.dto.review.ReplyOrderReviewResponse;
import com.berkay.order.service.domain.entity.OrderReview;
import com.berkay.order.service.domain.entity.Restaurant;
import com.berkay.order.service.domain.exception.OrderDomainException;
import com.berkay.order.service.domain.exception.ReviewNotFoundException;
import com.berkay.order.service.domain.mapper.OrderReviewDataMapper;
import com.berkay.order.service.domain.ports.output.repository.OrderReviewRepository;
import com.berkay.order.service.domain.ports.output.repository.RestaurantRepository;
import com.berkay.order.service.domain.valueobject.OrderReviewId;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
public class ReplyOrderReviewCommandHandler {

    private final OrderReviewRepository orderReviewRepository;
    private final RestaurantRepository restaurantRepository;
    private final OrderReviewDataMapper orderReviewDataMapper;

    public ReplyOrderReviewCommandHandler(OrderReviewRepository orderReviewRepository,
                                         RestaurantRepository restaurantRepository,
                                         OrderReviewDataMapper orderReviewDataMapper) {
        this.orderReviewRepository = orderReviewRepository;
        this.restaurantRepository = restaurantRepository;
        this.orderReviewDataMapper = orderReviewDataMapper;
    }

    @Transactional
    public ReplyOrderReviewResponse replyReview(ReplyOrderReviewCommand command) {
        log.info("Restaurant {} is replying to review {}", command.getRestaurantId(), command.getReviewId());

        OrderReview review = orderReviewRepository.findById(new OrderReviewId(command.getReviewId()))
                .orElseThrow(() -> new ReviewNotFoundException("Review not found with id: " + command.getReviewId()));

        if (!review.getRestaurantId().getValue().equals(command.getRestaurantId())) {
            log.warn("Review {} does not belong to restaurant {}", command.getReviewId(), command.getRestaurantId());
            throw new OrderDomainException("Review does not belong to restaurant: " + command.getRestaurantId());
        }

        Restaurant restaurant = restaurantRepository.findRestaurantByRestaurantId(command.getRestaurantId())
                .orElseThrow(() -> new OrderDomainException("Restaurant not found with id: " + command.getRestaurantId()));

        if (!restaurant.isActive()) {
            log.warn("Restaurant {} is inactive, cannot reply to review", command.getRestaurantId());
            throw new OrderDomainException("Cannot reply to reviews when restaurant is inactive!");
        }

        review.reply(command.getReply());
        OrderReview savedReview = orderReviewRepository.save(review);

        log.info("Restaurant {} successfully replied to review {}", command.getRestaurantId(), command.getReviewId());
        return orderReviewDataMapper.orderReviewToReplyOrderReviewResponse(savedReview, "Yorum yanıtınız başarıyla kaydedildi.");
    }
}
