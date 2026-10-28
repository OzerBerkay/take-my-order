package com.berkay.order.service.domain.dto.review;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class RestaurantReviewStatistics {
    private final long totalElements;
    private final long repliedCount;
    private final long pendingReplyCount;
}
