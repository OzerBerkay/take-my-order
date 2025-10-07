package com.berkay.restaurant.service.domain.ports.output.repository;

import com.berkay.restaurant.service.domain.entity.OrderApproval;

public interface OrderApprovalRepository {
    OrderApproval save(OrderApproval orderApproval);
}
