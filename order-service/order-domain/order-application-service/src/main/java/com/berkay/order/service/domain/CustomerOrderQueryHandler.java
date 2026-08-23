package com.berkay.order.service.domain;

import com.berkay.domain.valueobject.CustomerId;
import com.berkay.order.service.domain.dto.read.GetOrdersQuery;
import com.berkay.order.service.domain.dto.read.GetOrdersResponse;
import com.berkay.order.service.domain.dto.read.OrderSummary;
import com.berkay.order.service.domain.entity.Order;
import com.berkay.order.service.domain.ports.output.repository.OrderRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class CustomerOrderQueryHandler {

    private final OrderRepository orderRepository;

    public CustomerOrderQueryHandler(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Transactional(readOnly = true)
    public GetOrdersResponse getOrders(GetOrdersQuery getOrdersQuery) {
        com.berkay.order.service.domain.dto.read.OrderPageResult pageResult = orderRepository.findByCustomerId(
                new CustomerId(getOrdersQuery.getCustomerId()),
                getOrdersQuery.getPage(),
                getOrdersQuery.getSize()
        );

        List<OrderSummary> orderSummaries = pageResult.getOrders().stream()
                .map(this::mapToOrderSummary)
                .collect(Collectors.toList());

        return GetOrdersResponse.builder()
                .orders(orderSummaries)
                .page(pageResult.getPage())
                .size(pageResult.getSize())
                .totalElements(pageResult.getTotalElements())
                .totalPages(pageResult.getTotalPages())
                .isLast(pageResult.isLast())
                .build();
    }

    private OrderSummary mapToOrderSummary(Order order) {
        String failureMessages = order.getFailureMessages() != null ? String.join(", ", order.getFailureMessages()) : "";
        return OrderSummary.builder()
                .orderTrackingId(order.getTrackingId().getValue())
                .restaurantId(order.getRestaurantId().getValue())
                .orderStatus(order.getOrderStatus())
                .totalAmount(order.getPrice().getAmount())
                .failureMessages(failureMessages)
                .createdAt(order.getCreatedAt())
                .build();
    }
}
