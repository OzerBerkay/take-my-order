package com.berkay.order.service.domain;

import com.berkay.domain.valueobject.OrderId;
import com.berkay.domain.valueobject.OrderStatus;
import com.berkay.order.service.domain.dto.operation.ApproveOrderCommand;
import com.berkay.order.service.domain.dto.restaurant.RestaurantOrderResponse;
import com.berkay.order.service.domain.entity.Order;
import com.berkay.order.service.domain.exception.OrderConflictException;
import com.berkay.order.service.domain.exception.OrderDomainException;
import com.berkay.order.service.domain.exception.OrderNotFoundException;
import com.berkay.order.service.domain.mapper.OrderDataMapper;
import com.berkay.order.service.domain.ports.output.repository.OrderRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
public class OrderApproveCommandHandler {

    private final OrderRepository orderRepository;
    private final OrderDomainService orderDomainService;
    private final OrderDataMapper orderDataMapper;

    public OrderApproveCommandHandler(OrderRepository orderRepository,
                                      OrderDomainService orderDomainService,
                                      OrderDataMapper orderDataMapper) {
        this.orderRepository = orderRepository;
        this.orderDomainService = orderDomainService;
        this.orderDataMapper = orderDataMapper;
    }

    @Transactional
    public RestaurantOrderResponse approveOrder(ApproveOrderCommand command) {
        Order order = orderRepository.findByIdWithLock(new OrderId(command.getOrderId()))
                .orElseThrow(() -> new OrderNotFoundException("Order with id " + command.getOrderId() + " could not be found!"));

        if (order.getOrderStatus() != OrderStatus.WAITING_FOR_APPROVAL) {
            log.warn("Order with id: {} is not in WAITING_FOR_APPROVAL state! Current status: {}",
                    order.getId().getValue(), order.getOrderStatus());
            throw new OrderConflictException("Order is not in WAITING_FOR_APPROVAL state! Current state: " + order.getOrderStatus());
        }

        orderDomainService.approveOrder(order);
        orderRepository.save(order);
        log.info("Order with id: {} is approved by restaurant staff.", order.getId().getValue());

        return orderDataMapper.orderToRestaurantOrderResponse(order);
    }
}
