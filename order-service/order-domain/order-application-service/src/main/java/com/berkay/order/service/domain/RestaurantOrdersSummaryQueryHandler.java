package com.berkay.order.service.domain;

import com.berkay.domain.valueobject.RestaurantId;
import com.berkay.order.service.domain.dto.restaurant.RestaurantOrdersSummaryResponse;
import com.berkay.order.service.domain.ports.output.repository.OrderRepository;
import com.berkay.order.service.domain.ports.output.repository.RestaurantOrdersSummaryProjection;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Component
public class RestaurantOrdersSummaryQueryHandler {

    private final OrderRepository orderRepository;

    public RestaurantOrdersSummaryQueryHandler(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Transactional(readOnly = true)
    public RestaurantOrdersSummaryResponse getRestaurantOrdersSummary(UUID restaurantId) {
        log.info("Fetching order summary for restaurant: {}", restaurantId);

        RestaurantOrdersSummaryProjection projection = orderRepository.getRestaurantOrdersSummary(
                new RestaurantId(restaurantId));

        if (projection == null) {
            return RestaurantOrdersSummaryResponse.builder()
                    .totalCount(0)
                    .waitingCount(0)
                    .approvedCount(0)
                    .preparingCount(0)
                    .readyCount(0)
                    .inDeliveryCount(0)
                    .deliveredCount(0)
                    .cancelledCount(0)
                    .build();
        }

        return RestaurantOrdersSummaryResponse.builder()
                .totalCount(projection.getTotalCount())
                .waitingCount(projection.getWaitingCount())
                .approvedCount(projection.getApprovedCount())
                .preparingCount(projection.getPreparingCount())
                .readyCount(projection.getReadyCount())
                .inDeliveryCount(projection.getInDeliveryCount())
                .deliveredCount(projection.getDeliveredCount())
                .cancelledCount(projection.getCancelledCount())
                .build();
    }
}
