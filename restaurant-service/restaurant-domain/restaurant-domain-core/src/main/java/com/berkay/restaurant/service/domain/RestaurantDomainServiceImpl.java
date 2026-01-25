package com.berkay.restaurant.service.domain;

import com.berkay.domain.valueobject.OrderApprovalStatus;
import com.berkay.restaurant.service.domain.entity.Restaurant;
import com.berkay.restaurant.service.domain.event.OrderApprovalEvent;
import com.berkay.restaurant.service.domain.event.OrderApprovedEvent;
import com.berkay.restaurant.service.domain.event.OrderRejectedEvent;
import com.berkay.restaurant.service.domain.event.RestaurantInformationEvent;
import lombok.extern.slf4j.Slf4j;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import static com.berkay.domain.DomainConstants.UTC;

@Slf4j
public class RestaurantDomainServiceImpl implements RestaurantDomainService {

    @Override
    public OrderApprovalEvent validateOrder(Restaurant restaurant,
                                            List<String> failureMessages) {
        restaurant.validateOrder(failureMessages);
        log.info("Validating order with id: {}", restaurant.getOrderDetail().getId().getValue());

        if (failureMessages.isEmpty()) {
            log.info("Order is approved for order id: {}", restaurant.getOrderDetail().getId().getValue());
            restaurant.constructOrderApproval(OrderApprovalStatus.APPROVED);
            return new OrderApprovedEvent(restaurant.getOrderApproval(),
                    restaurant.getId(),
                    failureMessages,
                    ZonedDateTime.now(ZoneId.of(UTC)));
        } else {
            log.info("Order is rejected for order id: {}", restaurant.getOrderDetail().getId().getValue());
            restaurant.constructOrderApproval(OrderApprovalStatus.REJECTED);
            return new OrderRejectedEvent(restaurant.getOrderApproval(),
                    restaurant.getId(),
                    failureMessages,
                    ZonedDateTime.now(ZoneId.of(UTC)));
        }

    }

    @Override
    public RestaurantInformationEvent validateAndInitiateRestaurant(Restaurant restaurant) {
        // Entity üzerindeki initialize metodunu çağırıyoruz (ID atama, Active yapma)
        restaurant.initializeRestaurant();

        log.info("Restaurant with id: {} is initiated", restaurant.getId().getValue());

        // Event'i oluşturup dönüyoruz
        return new RestaurantInformationEvent(restaurant, ZonedDateTime.now(ZoneId.of(UTC)));
    }
}