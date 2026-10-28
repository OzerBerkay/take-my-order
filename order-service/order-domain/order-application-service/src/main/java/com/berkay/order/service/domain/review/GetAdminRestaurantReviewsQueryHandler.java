package com.berkay.order.service.domain.review;

import com.berkay.order.service.domain.dto.review.GetAdminRestaurantReviewsQuery;
import com.berkay.order.service.domain.dto.review.GetAdminRestaurantReviewsResponse;
import com.berkay.order.service.domain.dto.review.OrderReviewPageResult;
import com.berkay.order.service.domain.mapper.OrderReviewDataMapper;
import com.berkay.order.service.domain.ports.output.repository.OrderReviewRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
public class GetAdminRestaurantReviewsQueryHandler {

    private final OrderReviewRepository orderReviewRepository;
    private final OrderReviewDataMapper orderReviewDataMapper;

    public GetAdminRestaurantReviewsQueryHandler(OrderReviewRepository orderReviewRepository,
                                                OrderReviewDataMapper orderReviewDataMapper) {
        this.orderReviewRepository = orderReviewRepository;
        this.orderReviewDataMapper = orderReviewDataMapper;
    }

    @Transactional(readOnly = true)
    public GetAdminRestaurantReviewsResponse getAdminRestaurantReviews(GetAdminRestaurantReviewsQuery query) {
        log.info("Fetching admin reviews for restaurant: {}, status: {}, page: {}, size: {}",
                query.getRestaurantId(), query.getStatus(), query.getPage(), query.getSize());

        OrderReviewPageResult pageResult;
        String status = query.getStatus() != null ? query.getStatus().trim().toUpperCase() : "ALL";

        if ("ACTIVE".equals(status)) {
            pageResult = orderReviewRepository.findByRestaurantIdAndIsActive(query.getRestaurantId(), true, query.getPage(), query.getSize());
        } else if ("DELETED".equals(status)) {
            pageResult = orderReviewRepository.findByRestaurantIdAndIsActive(query.getRestaurantId(), false, query.getPage(), query.getSize());
        } else {
            pageResult = orderReviewRepository.findAllByRestaurantId(query.getRestaurantId(), query.getPage(), query.getSize());
        }

        return orderReviewDataMapper.orderReviewPageResultToGetAdminRestaurantReviewsResponse(pageResult);
    }
}
