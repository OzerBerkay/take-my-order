package com.berkay.restaurant.service.domain;

import com.berkay.domain.valueobject.OrderApprovalStatus;
import com.berkay.outbox.OutboxStatus;
import com.berkay.restaurant.service.domain.dto.approve.ApproveRestaurantOrderCommand;
import com.berkay.restaurant.service.domain.entity.OrderApproval;
import com.berkay.restaurant.service.domain.entity.Restaurant;
import com.berkay.restaurant.service.domain.event.OrderApprovedEvent;
import com.berkay.restaurant.service.domain.exception.RestaurantNotFoundException;
import com.berkay.restaurant.service.domain.mapper.RestaurantDataMapper;
import com.berkay.restaurant.service.domain.outbox.scheduler.OrderOutboxHelper;
import com.berkay.restaurant.service.domain.ports.output.repository.OrderApprovalRepository;
import com.berkay.restaurant.service.domain.ports.output.repository.RestaurantRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Component
public class ApproveRestaurantOrderCommandHandler {
    private final OrderApprovalRepository orderApprovalRepository;
    private final RestaurantRepository restaurantRepository;
    private final OrderOutboxHelper orderOutboxHelper;
    private final RestaurantDataMapper restaurantDataMapper;

    public ApproveRestaurantOrderCommandHandler(OrderApprovalRepository orderApprovalRepository,
                                                RestaurantRepository restaurantRepository,
                                                OrderOutboxHelper orderOutboxHelper,
                                                RestaurantDataMapper restaurantDataMapper) {
        this.orderApprovalRepository = orderApprovalRepository;
        this.restaurantRepository = restaurantRepository;
        this.orderOutboxHelper = orderOutboxHelper;
        this.restaurantDataMapper = restaurantDataMapper;
    }

    @Transactional
    public void approveOrder(ApproveRestaurantOrderCommand command) {
        Optional<OrderApproval> orderApprovalOpt = orderApprovalRepository.findByRestaurantIdAndOrderId(command.getRestaurantId(), command.getOrderId());
        if (orderApprovalOpt.isEmpty()) {
            throw new RestaurantNotFoundException("OrderApproval not found for restaurantId: " + command.getRestaurantId() + " and orderId: " + command.getOrderId());
        }
        OrderApproval orderApproval = orderApprovalOpt.get();
        Optional<Restaurant> restaurantResult = restaurantRepository.findRestaurantById(command.getRestaurantId());
        if (restaurantResult.isEmpty()) {
            throw new RestaurantNotFoundException("Restaurant not found for id: " + command.getRestaurantId());
        }
        Restaurant restaurant = restaurantResult.get();
        
        // Re-construct the aggregate root state to perform domain logic
        restaurant.setOrderApproval(orderApproval);
        
        OrderApprovedEvent event = restaurant.approveOrder();
        orderApprovalRepository.save(restaurant.getOrderApproval());
        
        orderOutboxHelper.saveOrderOutboxMessage(
                restaurantDataMapper.orderApprovalEventToOrderEventPayload(event),
                OrderApprovalStatus.APPROVED,
                OutboxStatus.STARTED,
                command.getOrderId() // Assuming sagaId maps to orderId for manual approval backward compat, wait!
        );
        log.info("Order {} is explicitly approved by restaurant {}", command.getOrderId(), command.getRestaurantId());
    }
}
