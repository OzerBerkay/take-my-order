package com.berkay.order.service.domain.ports.output.repository;

public interface CustomerOrdersSummaryProjection {
    long getAllCount();
    long getActiveCount();
    long getSucceededCount();
    long getAbortCount();
}
