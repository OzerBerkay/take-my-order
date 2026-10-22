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
import java.util.Optional;

import static com.berkay.domain.DomainConstants.UTC;

@Slf4j
public class RestaurantDomainServiceImpl implements RestaurantDomainService {

    @Override
    public Optional<OrderApprovalEvent> validateOrder(Restaurant restaurant,
                                            List<String> failureMessages) {
        return validateOrder(restaurant);
    }

    @Override
    public Optional<OrderApprovalEvent> validateOrder(Restaurant restaurant) {
        log.info("Validating order with id: {}", restaurant.getOrderDetail().getId().getValue());
        Optional<com.berkay.domain.valueobject.CancelReason> cancelReasonOpt = restaurant.validateOrder();

        if (cancelReasonOpt.isEmpty()) {
            log.info("Order is approved for order id: {}", restaurant.getOrderDetail().getId().getValue());
            return Optional.of(new OrderApprovedEvent(restaurant.getOrderApproval(),
                    restaurant.getId(),
                    new java.util.ArrayList<>(),
                    ZonedDateTime.now(ZoneId.of(UTC))));
        } else {
            com.berkay.domain.valueobject.CancelReason reason = cancelReasonOpt.get();
            log.info("Order is rejected with reason: {} for order id: {}", reason, restaurant.getOrderDetail().getId().getValue());
            restaurant.constructOrderApproval(OrderApprovalStatus.REJECTED);
            return Optional.of(new OrderRejectedEvent(restaurant.getOrderApproval(),
                    restaurant.getId(),
                    List.of(reason.name()),
                    reason,
                    ZonedDateTime.now(ZoneId.of(UTC))));
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