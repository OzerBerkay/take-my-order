package com.berkay.order.service.domain;

import com.berkay.domain.valueobject.OrderId;
import com.berkay.domain.valueobject.OrderStatus;
import com.berkay.order.service.domain.dto.operation.DeliverOrderCommand;
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
public class OrderDeliverCommandHandler {

    private final OrderRepository orderRepository;
    private final OrderDomainService orderDomainService;
    private final OrderSagaHelper orderSagaHelper;
    private final OrderDataMapper orderDataMapper;
    private final com.berkay.order.service.domain.outbox.scheduler.delivery.DeliveryOutboxHelper deliveryOutboxHelper;

    public OrderDeliverCommandHandler(OrderRepository orderRepository,
                                     OrderDomainService orderDomainService,
                                     OrderSagaHelper orderSagaHelper,
                                     OrderDataMapper orderDataMapper,
                                     com.berkay.order.service.domain.outbox.scheduler.delivery.DeliveryOutboxHelper deliveryOutboxHelper) {
        this.orderRepository = orderRepository;
        this.orderDomainService = orderDomainService;
        this.orderSagaHelper = orderSagaHelper;
        this.orderDataMapper = orderDataMapper;
        this.deliveryOutboxHelper = deliveryOutboxHelper;
    }

    @Transactional
    public RestaurantOrderResponse deliverOrder(DeliverOrderCommand command) {
        Order order = orderRepository.findByIdWithLock(new OrderId(command.getOrderId()))
                .orElseThrow(() -> new OrderNotFoundException("Order with id " + command.getOrderId() + " could not be found!"));

        if (order.getOrderStatus() != OrderStatus.IN_DELIVERY) {
            log.warn("Order with id: {} is not in IN_DELIVERY state! Current status: {}",
                    order.getId().getValue(), order.getOrderStatus());
            throw new OrderConflictException("Order is not in IN_DELIVERY state! Current state: " + order.getOrderStatus());
        }

        orderDomainService.deliver(order);
        orderSagaHelper.sealSagaAsSucceeded(order.getId().getValue());
        orderRepository.save(order);

        deliveryOutboxHelper.saveDeliveryOutboxMessage(
                com.berkay.order.service.domain.outbox.model.delivery.OrderDeliveredEventPayload.builder()
                        .orderId(order.getId().getValue())
                        .restaurantId(order.getRestaurantId().getValue())
                        .customerId(order.getCustomerId().getValue())
                        .price(order.getPrice().getAmount())
                        .createdAt(java.time.ZonedDateTime.now(java.time.ZoneId.of("UTC")))
                        .build(),
                com.berkay.outbox.OutboxStatus.STARTED);

        log.info("Order with id: {} is delivered, SAGA is sealed as SUCCEEDED, and delivery outbox message created.", order.getId().getValue());

        return orderDataMapper.orderToRestaurantOrderResponse(order);
    }
}
