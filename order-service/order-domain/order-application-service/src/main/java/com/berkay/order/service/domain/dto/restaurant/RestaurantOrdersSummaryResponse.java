package com.berkay.order.service.domain.dto.restaurant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class RestaurantOrdersSummaryResponse {
    private final long totalCount;
    private final long waitingCount;
    private final long approvedCount;
    private final long preparingCount;
    private final long readyCount;
    private final long inDeliveryCount;
    private final long deliveredCount;
    private final long cancelledCount;
}

