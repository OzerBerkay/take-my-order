package com.berkay.order.service.domain;

import com.berkay.order.service.domain.dto.query.PaidOrderItemResponse;
import com.berkay.order.service.domain.dto.query.PaidOrderResponse;
import com.berkay.order.service.domain.entity.Order;
import com.berkay.order.service.domain.ports.output.repository.OrderRepository;
import com.berkay.domain.valueobject.OrderStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Component
class RestaurantPaidOrderQueryHandler {

    private final OrderRepository orderRepository;

    public RestaurantPaidOrderQueryHandler(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public List<PaidOrderResponse> getPaidOrders(UUID restaurantId) {
        // Implement query logic to get paid orders for a given restaurant ID
        Optional<List<Order>> optionalOrders = orderRepository.findByRestaurantIdAndOrderStatus(new com.berkay.domain.valueobject.RestaurantId(restaurantId), OrderStatus.PAID);
        if (optionalOrders.isEmpty()) {
            log.warn("Could not find paid orders for restaurant: {}", restaurantId);
            return List.of();
        }
        
        return optionalOrders.get().stream()
                .map(order -> PaidOrderResponse.builder()
                        .orderId(order.getId().getValue())
                        .trackingId(order.getTrackingId().getValue())
                        .orderStatus(order.getOrderStatus().name())
                        .price(order.getPrice().getAmount())
                        .createdAt(null) // Order may not have createdAt mapped yet
                        .items(order.getItems().stream()
                                .map(item -> PaidOrderItemResponse.builder()
                                        .productId(item.getProduct().getId().getValue())
                                        .quantity(item.getQuantity())
                                        .price(item.getPrice().getAmount())
                                        .subTotal(item.getSubTotal().getAmount())
                                        .build())
                                .collect(Collectors.toList()))
                        .build())
                .collect(Collectors.toList());
    }
}
