package com.berkay.order.service.domain;

import com.berkay.domain.valueobject.OrderId;
import com.berkay.domain.valueobject.OrderStatus;
import com.berkay.order.service.domain.dto.operation.CancelOrderCommand;
import com.berkay.order.service.domain.dto.restaurant.RestaurantOrderResponse;
import com.berkay.order.service.domain.entity.Order;
import com.berkay.order.service.domain.exception.OrderConflictException;
import com.berkay.order.service.domain.exception.OrderNotFoundException;
import com.berkay.order.service.domain.mapper.OrderDataMapper;
import com.berkay.order.service.domain.ports.output.repository.OrderRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
public class OrderCancelCommandHandler {

    private final OrderRepository orderRepository;
    private final OrderCancelHelper orderCancelHelper;
    private final OrderDataMapper orderDataMapper;

    public OrderCancelCommandHandler(OrderRepository orderRepository,
                                     OrderCancelHelper orderCancelHelper,
                                     OrderDataMapper orderDataMapper) {
        this.orderRepository = orderRepository;
        this.orderCancelHelper = orderCancelHelper;
        this.orderDataMapper = orderDataMapper;
    }

    @Transactional
    public RestaurantOrderResponse cancelOrder(CancelOrderCommand command) {
        Order order = orderRepository.findByIdWithLock(new OrderId(command.getOrderId()))
                .orElseThrow(() -> new OrderNotFoundException("Order with id " + command.getOrderId() + " could not be found!"));

        // 1. İdempotent Güvenli Tekrar (Çift Tıklama / Ağ Tekrarı)
        if (order.getOrderStatus() == OrderStatus.CANCELLING || order.getOrderStatus() == OrderStatus.CANCELLED) {
            log.info("Order with id: {} is already in {} state. Returning idempotent HTTP 200 response.",
                    order.getId().getValue(), order.getOrderStatus());
            return orderDataMapper.orderToRestaurantOrderResponse(order);
        }

        // 2. Gerçek İleri Yön Çakışması (Başka bir kullanıcı hazırlığa/yola çıkardı)
        if (order.getOrderStatus() != command.getCurrentStatus()) {
            log.warn("Concurrency conflict for order id: {}. Client thought: {}, actual DB: {}",
                    order.getId().getValue(), command.getCurrentStatus(), order.getOrderStatus());
            throw new OrderConflictException("Sipariş durumu değişti (" + order.getOrderStatus() + "). Lütfen ekranı yenileyiniz.");
        }

        // 3. Normal İptal Akışı
        orderCancelHelper.initCancelOrder(order, command);
        return orderDataMapper.orderToRestaurantOrderResponse(order);
    }
}
