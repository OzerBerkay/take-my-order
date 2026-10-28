package com.berkay.order.service.domain.dto.review;

import com.berkay.order.service.domain.entity.OrderReview;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class OrderReviewPageResult {
    private final List<OrderReview> reviews;
    private final int page;
    private final int size;
    private final long totalElements;
    private final int totalPages;
    private final boolean isLast;
}
