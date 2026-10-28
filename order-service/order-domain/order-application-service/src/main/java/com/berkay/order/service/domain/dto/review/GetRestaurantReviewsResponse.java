package com.berkay.order.service.domain.dto.review;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetRestaurantReviewsResponse {
    private List<OrderReviewResponse> reviews;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private boolean isLast;
    private long repliedCount;
    private long pendingReplyCount;
}
