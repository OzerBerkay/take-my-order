package com.berkay.restaurant.service.domain;

import com.berkay.domain.valueobject.OrderApprovalStatus;
import com.berkay.outbox.OutboxStatus;
import com.berkay.restaurant.service.domain.dto.reject.RejectRestaurantOrderCommand;
import com.berkay.restaurant.service.domain.entity.OrderApproval;
import com.berkay.restaurant.service.domain.entity.Restaurant;
import com.berkay.restaurant.service.domain.event.OrderRejectedEvent;
import com.berkay.restaurant.service.domain.exception.RestaurantNotFoundException;
import com.berkay.restaurant.service.domain.mapper.RestaurantDataMapper;
import com.berkay.restaurant.service.domain.outbox.scheduler.OrderOutboxHelper;
import com.berkay.restaurant.service.domain.ports.output.repository.OrderApprovalRepository;
import com.berkay.restaurant.service.domain.ports.output.repository.RestaurantRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

@Slf4j
@Component
public class RejectRestaurantOrderCommandHandler {
    private final OrderApprovalRepository orderApprovalRepository;
    private final RestaurantRepository restaurantRepository;
    private final OrderOutboxHelper orderOutboxHelper;
    private final RestaurantDataMapper restaurantDataMapper;

    public RejectRestaurantOrderCommandHandler(OrderApprovalRepository orderApprovalRepository,
                                                RestaurantRepository restaurantRepository,
                                                OrderOutboxHelper orderOutboxHelper,
                                                RestaurantDataMapper restaurantDataMapper) {
        this.orderApprovalRepository = orderApprovalRepository;
        this.restaurantRepository = restaurantRepository;
        this.orderOutboxHelper = orderOutboxHelper;
        this.restaurantDataMapper = restaurantDataMapper;
    }

    @Transactional
    public void rejectOrder(RejectRestaurantOrderCommand command) {
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
        
        restaurant.setOrderApproval(orderApproval);
        
        // Sipariş iptal edildiğinde stok miktarını geri arttırıyoruz.
        if (orderApproval.getProductQuantities() != null) {
            java.util.Map<com.berkay.domain.valueobject.ProductId, com.berkay.restaurant.service.domain.entity.Product> restaurantMenu = 
                restaurant.getMenu().stream().collect(java.util.stream.Collectors.toMap(com.berkay.restaurant.service.domain.entity.Product::getId, java.util.function.Function.identity()));
                
            for (java.util.Map.Entry<com.berkay.domain.valueobject.ProductId, Integer> entry : orderApproval.getProductQuantities().entrySet()) {
                com.berkay.domain.valueobject.ProductId productId = entry.getKey();
                Integer requestedQuantity = entry.getValue();
                com.berkay.restaurant.service.domain.entity.Product menuProduct = restaurantMenu.get(productId);
                if (menuProduct != null) {
                    menuProduct.updateWith(menuProduct.getName(), menuProduct.getDescription(), menuProduct.getPrice(), menuProduct.isAvailable(), menuProduct.getStock() + requestedQuantity, menuProduct.isHidden(), menuProduct.getImageUrl());
                }
            }
            restaurantRepository.saveRestaurant(restaurant);
        }
        
        OrderRejectedEvent event = restaurant.rejectOrder(java.util.List.of("Order rejected by restaurant"));
        orderApprovalRepository.save(restaurant.getOrderApproval());
        
        orderOutboxHelper.saveOrderOutboxMessage(
                restaurantDataMapper.orderApprovalEventToOrderEventPayload(event),
                OrderApprovalStatus.REJECTED,
                OutboxStatus.STARTED,
                command.getOrderId() // Note: Using orderId as sagaId because the Outbox message links to the Saga
        );
        log.info("Order {} is explicitly rejected by restaurant {}", command.getOrderId(), command.getRestaurantId());
    }
}
