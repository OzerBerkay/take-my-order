package com.berkay.order.service.domain;

import com.berkay.order.service.domain.dto.restaurant.PlatformOrderQuery;
import com.berkay.order.service.domain.dto.restaurant.RestaurantOrderResponse;
import com.berkay.order.service.domain.entity.Order;
import com.berkay.order.service.domain.exception.OrderNotFoundException;
import com.berkay.order.service.domain.mapper.OrderDataMapper;
import com.berkay.order.service.domain.ports.output.repository.OrderRepository;
import com.berkay.order.service.domain.valueobject.TrackingId;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
public class PlatformOrderQueryHandler {

    private final OrderRepository orderRepository;
    private final OrderDataMapper orderDataMapper;

    public PlatformOrderQueryHandler(OrderRepository orderRepository,
                                    OrderDataMapper orderDataMapper) {
        this.orderRepository = orderRepository;
        this.orderDataMapper = orderDataMapper;
    }

    @Transactional(readOnly = true)
    public RestaurantOrderResponse getPlatformOrder(PlatformOrderQuery query) {
        Order order = orderRepository.findByTrackingId(new TrackingId(query.getTrackingId()))
                .orElseThrow(() -> new OrderNotFoundException("Order with tracking id " + query.getTrackingId() + " could not be found!"));

        return orderDataMapper.orderToRestaurantOrderResponse(order);
    }
}
