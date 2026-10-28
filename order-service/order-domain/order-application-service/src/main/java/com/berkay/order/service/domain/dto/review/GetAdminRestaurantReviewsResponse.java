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
public class GetAdminRestaurantReviewsResponse {
    private List<AdminOrderReviewResponse> reviews;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private boolean isLast;
}
