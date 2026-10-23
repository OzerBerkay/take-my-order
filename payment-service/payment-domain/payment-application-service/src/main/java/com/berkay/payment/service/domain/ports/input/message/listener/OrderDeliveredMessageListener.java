package com.berkay.payment.service.domain.ports.input.message.listener;

import com.berkay.payment.service.domain.dto.OrderDeliveredRequest;

public interface OrderDeliveredMessageListener {
    void settleOrder(OrderDeliveredRequest orderDeliveredRequest);
}
