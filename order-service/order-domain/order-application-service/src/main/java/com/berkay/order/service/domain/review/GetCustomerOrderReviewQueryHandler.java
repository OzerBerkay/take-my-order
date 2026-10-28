package com.berkay.order.service.domain.review;

import com.berkay.order.service.domain.dto.review.CustomerOrderReviewResponse;
import com.berkay.order.service.domain.entity.OrderReview;
import com.berkay.order.service.domain.exception.ReviewNotFoundException;
import com.berkay.order.service.domain.mapper.OrderReviewDataMapper;
import com.berkay.order.service.domain.ports.output.repository.OrderReviewRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Component
public class GetCustomerOrderReviewQueryHandler {

    private final OrderReviewRepository orderReviewRepository;
    private final OrderReviewDataMapper orderReviewDataMapper;

    public GetCustomerOrderReviewQueryHandler(OrderReviewRepository orderReviewRepository,
                                             OrderReviewDataMapper orderReviewDataMapper) {
        this.orderReviewRepository = orderReviewRepository;
        this.orderReviewDataMapper = orderReviewDataMapper;
    }

    @Transactional(readOnly = true)
    public CustomerOrderReviewResponse getReview(UUID orderId) {
        log.info("Fetching review for order id: {}", orderId);
        OrderReview review = orderReviewRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ReviewNotFoundException("No review found for order id: " + orderId));
        return orderReviewDataMapper.orderReviewToCustomerOrderReviewResponse(review);
    }
}
