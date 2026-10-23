package com.berkay.order.service.domain;

import com.berkay.domain.valueobject.OrderId;
import com.berkay.order.service.domain.dto.restaurant.OrderDetailQuery;
import com.berkay.order.service.domain.dto.restaurant.RestaurantOrderResponse;
import com.berkay.order.service.domain.entity.Order;
import com.berkay.order.service.domain.exception.OrderNotFoundException;
import com.berkay.order.service.domain.mapper.OrderDataMapper;
import com.berkay.order.service.domain.ports.output.repository.OrderRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
public class OrderDetailQueryHandler {

    private final OrderRepository orderRepository;
    private final OrderDataMapper orderDataMapper;

    public OrderDetailQueryHandler(OrderRepository orderRepository,
                                  OrderDataMapper orderDataMapper) {
        this.orderRepository = orderRepository;
        this.orderDataMapper = orderDataMapper;
    }

    @Transactional(readOnly = true)
    public RestaurantOrderResponse getOrderDetail(OrderDetailQuery query) {
        Order order = orderRepository.findById(new OrderId(query.getOrderId()))
                .orElseThrow(() -> new OrderNotFoundException("Order with id " + query.getOrderId() + " could not be found!"));

        return orderDataMapper.orderToRestaurantOrderResponse(order);
    }
}
