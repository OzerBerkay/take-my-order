package com.berkay.order.service.domain.review;

import com.berkay.order.service.domain.dto.review.GetRestaurantReviewsQuery;
import com.berkay.order.service.domain.dto.review.GetRestaurantReviewsResponse;
import com.berkay.order.service.domain.dto.review.OrderReviewPageResult;
import com.berkay.order.service.domain.mapper.OrderReviewDataMapper;
import com.berkay.order.service.domain.ports.output.repository.OrderReviewRepository;
import com.berkay.order.service.domain.dto.review.RestaurantReviewStatistics;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
public class GetRestaurantReviewsQueryHandler {

    private final OrderReviewRepository orderReviewRepository;
    private final OrderReviewDataMapper orderReviewDataMapper;

    public GetRestaurantReviewsQueryHandler(OrderReviewRepository orderReviewRepository,
                                           OrderReviewDataMapper orderReviewDataMapper) {
        this.orderReviewRepository = orderReviewRepository;
        this.orderReviewDataMapper = orderReviewDataMapper;
    }

    @Transactional(readOnly = true)
    public GetRestaurantReviewsResponse getRestaurantReviews(GetRestaurantReviewsQuery query) {
        log.info("Fetching reviews for restaurant: {}, status: {}, page: {}, size: {}",
                query.getRestaurantId(), query.getStatus(), query.getPage(), query.getSize());
        OrderReviewPageResult pageResult = orderReviewRepository.findActiveByRestaurantIdAndStatus(
                query.getRestaurantId(), query.getStatus(), query.getPage(), query.getSize());
        RestaurantReviewStatistics statistics = orderReviewRepository.getReviewStatistics(query.getRestaurantId());
        return orderReviewDataMapper.orderReviewPageResultToGetRestaurantReviewsResponse(
                pageResult, statistics.getRepliedCount(), statistics.getPendingReplyCount());
    }
}
