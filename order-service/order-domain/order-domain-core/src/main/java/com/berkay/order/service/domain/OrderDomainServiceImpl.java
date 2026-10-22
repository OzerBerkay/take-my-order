package com.berkay.order.service.domain;

import com.berkay.order.service.domain.entity.Order;
import com.berkay.order.service.domain.entity.Product;
import com.berkay.order.service.domain.entity.Restaurant;
import com.berkay.domain.valueobject.Money;
import com.berkay.order.service.domain.event.OrderCancelledEvent;
import com.berkay.order.service.domain.event.OrderCreatedEvent;
import com.berkay.order.service.domain.event.OrderPaidEvent;
import com.berkay.order.service.domain.exception.OrderDomainException;
import lombok.extern.slf4j.Slf4j;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.berkay.domain.DomainConstants.UTC;

@Slf4j
public class OrderDomainServiceImpl implements OrderDomainService {

    // Product id and item price comes from client. To be sure it is the real price of product
    // we need to validate with Restaurant entity
    @Override
    public OrderCreatedEvent validateAndInitiateOrder(Order order, Restaurant restaurant) {
        validateRestaurant(restaurant);
        validateAndSetOrderProductInformation(order, restaurant);
                Money itemsTotal = order.getItems().stream().map(orderItem -> {
            
            return orderItem.getSubTotal();
        }).reduce(com.berkay.domain.valueobject.Money.ZERO, com.berkay.domain.valueobject.Money::add);
        
        if (restaurant.getMinimumOrderAmount() != null && restaurant.getMinimumOrderAmount().isGreaterThan(itemsTotal)) {
            throw new OrderDomainException("Order amount is less than minimum order amount!");
        }
        
        boolean deliveryFeeMatches;
        if (restaurant.getDeliveryFee() == null) {
            // If restaurant replica has no delivery fee, we assume 0
            deliveryFeeMatches = order.getDeliveryFee() == null || order.getDeliveryFee().getAmount().compareTo(java.math.BigDecimal.ZERO) == 0;
        } else {
            deliveryFeeMatches = order.getDeliveryFee() != null && order.getDeliveryFee().equals(restaurant.getDeliveryFee());
        }

        if (!deliveryFeeMatches) {
            String customerFee = order.getDeliveryFee() != null ? order.getDeliveryFee().getAmount().toString() : "null";
            String restaurantFee = restaurant.getDeliveryFee() != null ? restaurant.getDeliveryFee().getAmount().toString() : "0";
            throw new OrderDomainException("Delivery fee mismatch! Customer provided: " + customerFee
                + ", but restaurant requires: " + restaurantFee);
        }

        order.validateOrder(restaurant.getDeliveryFee());
        order.initializeOrder();
        log.info("Order with id: {} is initiated", order.getId().getValue());
        return new OrderCreatedEvent(order, ZonedDateTime.now(ZoneId.of(UTC)));
    }

    @Override
    public OrderPaidEvent payOrder(Order order) {
        order.pay();
        log.info("Order with id: {} is paid", order.getId().getValue());
        return new OrderPaidEvent(order, ZonedDateTime.now(ZoneId.of(UTC)));
    }

    @Override
    public void waitForApproval(Order order) {
        order.waitForApproval();
        log.info("Order with id: {} is waiting for approval", order.getId().getValue());
    }

    @Override
    public void approveOrder(Order order) {
        order.approve();
        log.info("Order with id: {} is approved", order.getId().getValue());
    }

    @Override
    public void startPreparing(Order order) {
        order.startPreparing();
        log.info("Order with id: {} started preparing", order.getId().getValue());
    }

    @Override
    public void readyForPickup(Order order) {
        order.readyForPickup();
        log.info("Order with id: {} is ready for pickup", order.getId().getValue());
    }

    @Override
    public void startDelivery(Order order) {
        order.startDelivery();
        log.info("Order with id: {} started delivery", order.getId().getValue());
    }

    @Override
    public void deliver(Order order) {
        order.deliver();
        log.info("Order with id: {} is delivered", order.getId().getValue());
    }

    @Override
    public OrderCancelledEvent cancelOrderPayment(Order order, List<String> failureMessages) {
        order.initCancel(failureMessages);
        log.info("Order payment with id: {} is cancelling", order.getId().getValue());
        return new OrderCancelledEvent(order, ZonedDateTime.now(ZoneId.of(UTC)));
    }

    @Override
    public OrderCancelledEvent cancelOrderPayment(Order order, com.berkay.domain.valueobject.CancelReason cancelReason, com.berkay.domain.valueobject.MaterialAction materialAction, String internalNote) {
        order.initCancel(cancelReason, materialAction, internalNote);
        log.info("Order payment with id: {} is cancelling with reason: {}", order.getId().getValue(), cancelReason);
        return new OrderCancelledEvent(order, ZonedDateTime.now(ZoneId.of(UTC)));
    }

    @Override
    public void cancelOrder(Order order, List<String> failureMessages) {
        order.cancel(failureMessages);
        log.info("Order with id: {} is cancelled", order.getId().getValue());
    }

    @Override
    public void cancelOrder(Order order, com.berkay.domain.valueobject.CancelReason cancelReason) {
        order.cancel(cancelReason);
        log.info("Order with id: {} is cancelled with reason: {}", order.getId().getValue(), cancelReason);
    }

    @Override
    public void cancelOrderWithoutRefund(Order order, com.berkay.domain.valueobject.CancelReason cancelReason, com.berkay.domain.valueobject.MaterialAction materialAction, String internalNote) {
        order.cancelWithoutRefund(cancelReason, materialAction, internalNote);
        log.info("Order with id: {} cancelled without refund with reason: {}", order.getId().getValue(), cancelReason);
    }

    private void validateRestaurant(Restaurant restaurant) {
        if (!restaurant.isActive()) {
            throw new OrderDomainException("Restaurant with id " + restaurant.getId().getValue()
                    + " is currently not active");
        }
        if (!restaurant.isAvailable()) {
            throw new OrderDomainException("Restaurant with id " + restaurant.getId().getValue()
                    + " is currently not available");
        }
    }

    // Ensuring order product information from client is same with Restaurant's product
    private void validateAndSetOrderProductInformation(Order order, Restaurant restaurant) {
        // O(N) Performans için Map
        Map<UUID, Product> restaurantProductMap = restaurant.getProducts().stream()
                .collect(Collectors.toMap(p -> p.getId().getValue(), p -> p));

        order.getItems().forEach(orderItem -> {
            Product restaurantProduct = restaurantProductMap.get(orderItem.getProduct().getId().getValue());

            // Helper kontrol ettiği için null gelmez ama "sigorta" olarak kalsın.
            if (restaurantProduct != null) {

                // Ürün satışa açık mı?
                if (!restaurantProduct.isAvailable()) {
                    throw new OrderDomainException("Product " + restaurantProduct.getId().getValue() + " is not available.");
                }

                // Ürün gizli mi?
                if (restaurantProduct.isHidden()) {
                    throw new OrderDomainException("Product " + restaurantProduct.getId().getValue() + " is hidden and cannot be ordered.");
                }

                // Product güncellenir çünkü tüm product'ı dto'da almadık sadece id aldık
                // DB'den product'ı alarak entity içinde validasyonları sağlayacağız
                orderItem.getProduct().updateWithConfirmedNamePriceAndAvailability(
                        restaurantProduct.getName(),
                        restaurantProduct.getPrice(),
                        restaurantProduct.isAvailable(),
                        restaurantProduct.isHidden());
            } else {
                throw new OrderDomainException("Product with id: " + orderItem.getProduct().getId().getValue() + " not found in restaurant");
            }
        });
    }
}
