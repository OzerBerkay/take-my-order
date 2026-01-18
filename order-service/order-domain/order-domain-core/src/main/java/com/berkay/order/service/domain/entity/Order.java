package com.berkay.order.service.domain.entity;

import com.berkay.domain.entity.AggregateRoot;
import com.berkay.domain.valueobject.*;
import com.berkay.order.service.domain.exception.OrderDomainException;
import com.berkay.order.service.domain.valueobject.OrderItemId;
import com.berkay.order.service.domain.valueobject.StreetAddress;
import com.berkay.order.service.domain.valueobject.TrackingId;

import java.util.List;
import java.util.UUID;

// Order is an entity class.
// Entity classes contains the methods to complete critical business rules and can act as an
// aggregate root, and in that case forcing all business invariants is the responsibility of that entity.
public class Order extends AggregateRoot<OrderId> {
    // Comes from outside
    // These are final because fixed on first order request.
    private final CustomerId customerId;
    private final RestaurantId restaurantId;
    private final StreetAddress deliveryAddress;
    private final Money price;
    private final List<OrderItem> items;

    // Will be described in aggregate root
    private TrackingId trackingId;
    // With the order status, saga states can be tracked
    private OrderStatus orderStatus;
    // Will be described later if needed
    private List<String> failureMessages;

    public static final String FAILURE_MESSAGE_DELIMITER = ",";

    public void initializeOrder() {
        setId(new OrderId(UUID.randomUUID()));
        trackingId = new TrackingId(UUID.randomUUID());
        orderStatus = OrderStatus.PENDING;
        initializeOrderItems();
    }

    public void validateOrder() {
        validateInitialOrder();
        validateTotalPrice();
        validateItemsPrice();
    }

    public void pay() {
        if (orderStatus != OrderStatus.PENDING) {
            throw new OrderDomainException("Order is not in correct state for payment");
        }
        // If order status is pending, it means we can pay
        orderStatus = OrderStatus.PAID;
    }

    // If price paid, then restaurant checks if order payment is approved
    public void approve() {
        if (orderStatus != OrderStatus.PAID) {
            throw new OrderDomainException("Order is not in correct state for approval");
        }

        orderStatus = OrderStatus.APPROVED;
    }

    // initCancel and cancel methods are for saga compensating methods
    // initCancel comes after payment issue, Cancel comes after cancel operation in pending state that way before attempting to pay
    public void initCancel(List<String> failureMessages) {
        if (orderStatus != OrderStatus.PAID) {
            throw new OrderDomainException("Order is not in correct state for initCancel operation!");
        }

        orderStatus = OrderStatus.CANCELLING;
        updateFailureMessages(failureMessages);
    }

    // To set cancelled status, present status must be Cancelling or Pending, so it means payment never completed
    public void cancel(List<String> failureMessages) {
        if (!(orderStatus == OrderStatus.CANCELLING || orderStatus == OrderStatus.PENDING)) {
            throw new OrderDomainException("Order is not in correct state for cancel operation!");
        }
        orderStatus = OrderStatus.CANCELLED;
        updateFailureMessages(failureMessages);
    }

    private void updateFailureMessages(List<String> failureMessages) {
        if (this.failureMessages != null && failureMessages != null) {
            this.failureMessages.addAll(failureMessages.stream().filter(message -> !message.isEmpty()).toList());
        }
        if (failureMessages == null) {
            this.failureMessages = failureMessages;
        }
    }


    private void validateItemsPrice() {
        Money orderItemsTotal = items.stream().map(orderItem -> {
            orderItem.validatePrice();
            return orderItem.getSubTotal(); // gets all order item's subtotal in map
        }).reduce(Money.ZERO, Money::add); // add all order items' subtotal to get order items total so it reduces map to one value

        if (!price.equals(orderItemsTotal)) {
            throw new OrderDomainException("Total price (" + price.getAmount()
                    + ") is not equal to order items total (" + orderItemsTotal.getAmount() + ")!");
        }
    }

    private void validateTotalPrice() {
        if (price == null || !price.isGreaterThanZero()) {
            throw new OrderDomainException("Total price must be greater than zero!");
        }
    }

    private void validateInitialOrder() {
        if (orderStatus != null || getId() != null) {
            throw new OrderDomainException("Order is not in correct state for initialization");
        }
    }

    // Each order item will have a unique id {OrderId, OrderItemId}, because to distinguish
    private void initializeOrderItems() {
        long itemId = 1;
        for (OrderItem orderItem : items) {
            // Until here order item doesn't have an orderId and orderItemId yet
            // With this definition we can know which order item belongs to which order
            orderItem.initializeOrderItem(super.getId(), new OrderItemId(itemId++));
        }
    }

    private Order(Builder builder) {
        super.setId(builder.orderId);
        customerId = builder.customerId;
        restaurantId = builder.restaurantId;
        deliveryAddress = builder.deliveryAddress;
        price = builder.price;
        items = builder.items;
        trackingId = builder.trackingId;
        orderStatus = builder.orderStatus;
        failureMessages = builder.failureMessages;
    }


    public CustomerId getCustomerId() {
        return customerId;
    }

    public RestaurantId getRestaurantId() {
        return restaurantId;
    }

    public StreetAddress getDeliveryAddress() {
        return deliveryAddress;
    }

    public Money getPrice() {
        return price;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public TrackingId getTrackingId() {
        return trackingId;
    }

    public OrderStatus getOrderStatus() {
        return orderStatus;
    }

    public List<String> getFailureMessages() {
        return failureMessages;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private OrderId orderId;
        private CustomerId customerId;
        private RestaurantId restaurantId;
        private StreetAddress deliveryAddress;
        private Money price;
        private List<OrderItem> items;
        private TrackingId trackingId;
        private OrderStatus orderStatus;
        private List<String> failureMessages;

        private Builder() {
        }

        public Builder orderId(OrderId val) {
            orderId = val;
            return this;
        }

        public Builder customerId(CustomerId val) {
            customerId = val;
            return this;
        }

        public Builder restaurantId(RestaurantId val) {
            restaurantId = val;
            return this;
        }

        public Builder deliveryAddress(StreetAddress val) {
            deliveryAddress = val;
            return this;
        }

        public Builder price(Money val) {
            price = val;
            return this;
        }

        public Builder items(List<OrderItem> val) {
            items = val;
            return this;
        }

        public Builder trackingId(TrackingId val) {
            trackingId = val;
            return this;
        }

        public Builder orderStatus(OrderStatus val) {
            orderStatus = val;
            return this;
        }

        public Builder failureMessages(List<String> val) {
            failureMessages = val;
            return this;
        }

        public Order build() {
            return new Order(this);
        }
    }
}
