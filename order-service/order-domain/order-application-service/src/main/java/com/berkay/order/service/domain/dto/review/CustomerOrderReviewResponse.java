package com.berkay.order.service.domain.dto.review;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerOrderReviewResponse {
    private UUID reviewId;
    private UUID orderId;
    private UUID restaurantId;
    private int rating;
    private String comment;
    private String customerNameMasked;
    private String restaurantReply;
    private ZonedDateTime restaurantRepliedAt;
    private ZonedDateTime createdAt;
}
