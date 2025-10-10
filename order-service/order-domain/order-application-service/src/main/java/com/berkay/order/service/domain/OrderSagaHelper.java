package com.berkay.order.service.domain;

import com.berkay.domain.valueobject.OrderId;
import com.berkay.order.service.domain.entity.Order;
import com.berkay.order.service.domain.exception.OrderNotFoundException;
import com.berkay.order.service.domain.ports.output.repository.OrderRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Component
public class OrderSagaHelper {

    private final OrderRepository orderRepository;

    public OrderSagaHelper(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public Order findOrder(String orderId) {
        Optional<Order> optionalOrder = orderRepository.findById(new OrderId(UUID.fromString(orderId)));
        if (optionalOrder.isEmpty()) {
            log.error("Order with id: {} could not be found!", orderId);
            throw new OrderNotFoundException("Order with id " + orderId + " could not be found!");
        }
        return optionalOrder.get();
    }

    void saveOrder(Order order) {
        orderRepository.save(order);
    }
}
