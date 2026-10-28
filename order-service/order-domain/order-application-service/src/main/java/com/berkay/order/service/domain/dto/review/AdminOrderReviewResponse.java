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
public class AdminOrderReviewResponse {
    private UUID reviewId;
    private UUID orderId;
    private UUID customerId;
    private UUID restaurantId;
    private int rating;
    private String comment;
    private String customerNameMasked;
    private String restaurantReply;
    private ZonedDateTime restaurantRepliedAt;
    private boolean isActive;
    private String deactivationReason;
    private ZonedDateTime deactivatedAt;
    private UUID deactivatedBy;
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;
}
