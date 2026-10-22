package com.berkay.order.service.domain;

import com.berkay.domain.valueobject.OrderId;
import com.berkay.domain.valueobject.OrderStatus;
import com.berkay.order.service.domain.dto.operation.ReadyForPickupOrderCommand;
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
public class OrderReadyForPickupCommandHandler {

    private final OrderRepository orderRepository;
    private final OrderDomainService orderDomainService;
    private final OrderDataMapper orderDataMapper;

    public OrderReadyForPickupCommandHandler(OrderRepository orderRepository,
                                             OrderDomainService orderDomainService,
                                             OrderDataMapper orderDataMapper) {
        this.orderRepository = orderRepository;
        this.orderDomainService = orderDomainService;
        this.orderDataMapper = orderDataMapper;
    }

    @Transactional
    public RestaurantOrderResponse readyForPickupOrder(ReadyForPickupOrderCommand command) {
        Order order = orderRepository.findByIdWithLock(new OrderId(command.getOrderId()))
                .orElseThrow(() -> new OrderNotFoundException("Order with id " + command.getOrderId() + " could not be found!"));

        if (order.getOrderStatus() != OrderStatus.PREPARING) {
            log.warn("Order with id: {} is not in PREPARING state! Current status: {}",
                    order.getId().getValue(), order.getOrderStatus());
            throw new OrderConflictException("Order is not in PREPARING state! Current state: " + order.getOrderStatus());
        }

        orderDomainService.readyForPickup(order);
        orderRepository.save(order);
        log.info("Order with id: {} is ready for pickup.", order.getId().getValue());

        return orderDataMapper.orderToRestaurantOrderResponse(order);
    }
}
