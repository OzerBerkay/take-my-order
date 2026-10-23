package com.berkay.order.service.domain.ports.output.repository;

public interface RestaurantOrdersSummaryProjection {
    long getTotalCount();
    long getWaitingCount();
    long getApprovedCount();
    long getPreparingCount();
    long getReadyCount();
    long getInDeliveryCount();
    long getDeliveredCount();
    long getCancelledCount();
}

