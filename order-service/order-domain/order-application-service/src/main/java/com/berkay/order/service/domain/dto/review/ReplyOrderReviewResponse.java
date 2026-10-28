package com.berkay.order.service.domain.dto.review;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReplyOrderReviewResponse {
    private UUID reviewId;
    private String message;
}
