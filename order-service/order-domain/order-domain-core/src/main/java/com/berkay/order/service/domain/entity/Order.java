package com.berkay.order.service.domain.entity;

import com.berkay.domain.entity.AggregateRoot;
import com.berkay.domain.valueobject.*;
import com.berkay.order.service.domain.exception.OrderDomainException;
import com.berkay.order.service.domain.valueobject.OrderItemId;
import com.berkay.order.service.domain.valueobject.StreetAddress;
import com.berkay.order.service.domain.valueobject.TrackingId;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Order extends AggregateRoot<OrderId> {
    public static final String FAILURE_MESSAGE_DELIMITER = ",";
    private final CustomerId customerId;
    private final RestaurantId restaurantId;
    private final StreetAddress deliveryAddress;
    private final Money price;
    private final Money deliveryFee;
    private final List<OrderItem> items;

    private TrackingId trackingId;
    private OrderStatus orderStatus;
    private java.time.ZonedDateTime createdAt;
    private java.time.ZonedDateTime deliveredAt;
    private boolean isReviewed;

    private CancelReason cancelReason;
    private MaterialAction materialAction;
    private String internalNote;
    private Integer version;

    public void initializeOrder() {
        setId(new OrderId(UUID.randomUUID()));
        trackingId = new TrackingId(UUID.randomUUID());
        orderStatus = OrderStatus.PENDING;
        createdAt = java.time.ZonedDateTime.now(java.time.ZoneId.of("UTC"));
        initializeOrderItems();
    }

    public void validateOrder(Money deliveryFee) {
        validateInitialOrder();
        validateTotalPrice();
        validateItemsPrice(deliveryFee);
    }

    public void pay() {
        if (orderStatus != OrderStatus.PENDING) {
            throw new OrderDomainException("Order is not in correct state for payment");
        }
        orderStatus = OrderStatus.PAID;
    }

    public void waitForApproval() {
        if (orderStatus != OrderStatus.PAID) {
            throw new OrderDomainException("Order is not in correct state for waiting for approval! Current state: " + orderStatus);
        }
        orderStatus = OrderStatus.WAITING_FOR_APPROVAL;
    }

    public void approve() {
        if (orderStatus != OrderStatus.WAITING_FOR_APPROVAL) {
            throw new OrderDomainException("Order is not in correct state for approval! Current state: " + orderStatus);
        }
        orderStatus = OrderStatus.APPROVED;
    }

    public void startPreparing() {
        if (orderStatus != OrderStatus.APPROVED) {
            throw new OrderDomainException("Order is not in correct state for preparing! Current state: " + orderStatus);
        }
        orderStatus = OrderStatus.PREPARING;
    }

    public void readyForPickup() {
        if (orderStatus != OrderStatus.PREPARING) {
            throw new OrderDomainException("Order is not in correct state for ready for pickup! Current state: " + orderStatus);
        }
        orderStatus = OrderStatus.READY_FOR_PICKUP;
    }

    public void startDelivery() {
        if (orderStatus != OrderStatus.READY_FOR_PICKUP) {
            throw new OrderDomainException("Order is not in correct state for delivery! Current state: " + orderStatus);
        }
        orderStatus = OrderStatus.IN_DELIVERY;
    }

    public void deliver() {
        if (orderStatus != OrderStatus.IN_DELIVERY) {
            throw new OrderDomainException("Order is not in correct state for deliver operation! Current state: " + orderStatus);
        }
        orderStatus = OrderStatus.DELIVERED;
        deliveredAt = java.time.ZonedDateTime.now(java.time.ZoneId.of("UTC"));
    }

    public void initCancel(CancelReason cancelReason, MaterialAction materialAction, String internalNote) {
        boolean isCancellableState = orderStatus == OrderStatus.PAID ||
                orderStatus == OrderStatus.WAITING_FOR_APPROVAL ||
                orderStatus == OrderStatus.APPROVED ||
                orderStatus == OrderStatus.PREPARING ||
                orderStatus == OrderStatus.READY_FOR_PICKUP ||
                orderStatus == OrderStatus.IN_DELIVERY;
        if (!isCancellableState) {
            throw new OrderDomainException("Order is not in correct state for initCancel operation! Current state: " + orderStatus);
        }
        if (cancelReason == CancelReason.INSUFFICIENT_STOCK && materialAction == MaterialAction.RESTORE_STOCK) {
            throw new OrderDomainException("Cannot restore stock when cancellation reason is INSUFFICIENT_STOCK! Must be RECORD_WASTE.");
        }
        this.orderStatus = OrderStatus.CANCELLING;
        this.cancelReason = cancelReason;
        this.materialAction = materialAction;
        this.internalNote = internalNote;
    }

    public void initCancel(List<String> failureMessages) {
        initCancel(null, null, failureMessages != null ? String.join(",", failureMessages) : null);
    }

    public void cancelWithoutRefund(CancelReason cancelReason, MaterialAction materialAction, String internalNote) {
        if (orderStatus != OrderStatus.IN_DELIVERY) {
            throw new OrderDomainException("Order is not in correct state for no-refund cancellation! Current state: " + orderStatus);
        }
        if (cancelReason != CancelReason.CUSTOMER_UNREACHABLE && cancelReason != CancelReason.WRONG_ADDRESS) {
            throw new OrderDomainException("CancelReason " + cancelReason + " is not eligible for no-refund cancellation!");
        }
        this.orderStatus = OrderStatus.CANCELLED;
        this.cancelReason = cancelReason;
        this.materialAction = (materialAction != null ? materialAction : MaterialAction.RECORD_WASTE);
        this.internalNote = internalNote;
    }

    public void cancel(CancelReason cancelReason) {
        if (!(orderStatus == OrderStatus.CANCELLING || orderStatus == OrderStatus.PENDING)) {
            throw new OrderDomainException("Order is not in correct state for cancel operation! Current state: " + orderStatus);
        }
        this.orderStatus = OrderStatus.CANCELLED;
        if (cancelReason != null) {
            this.cancelReason = cancelReason;
        }
    }

    public void cancel(List<String> failureMessages) {
        cancel(this.cancelReason);
        if (failureMessages != null && !failureMessages.isEmpty() && this.internalNote == null) {
            this.internalNote = String.join(",", failureMessages);
        }
    }

    public void setCancelMetadata(CancelReason cancelReason, MaterialAction materialAction, String internalNote) {
        this.cancelReason = cancelReason;
        this.materialAction = materialAction;
        this.internalNote = internalNote;
    }

    public void validateItemsPrice(Money deliveryFee) {
        Money orderItemsTotal = items.stream().map(orderItem -> {
            orderItem.validatePrice();
            return orderItem.getSubTotal();
        }).reduce(Money.ZERO, Money::add);

        Money expectedTotal = orderItemsTotal.add(deliveryFee != null ? deliveryFee : Money.ZERO);

        if (!price.equals(expectedTotal)) {
            throw new OrderDomainException("Total price (" + price.getAmount()
                    + ") not equal to total item price (" + orderItemsTotal.getAmount()
                    + ") + delivery fee (" + (deliveryFee != null ? deliveryFee.getAmount() : "0")
                    + ") = " + expectedTotal.getAmount());
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

    private void initializeOrderItems() {
        long itemId = 1;
        for (OrderItem orderItem : items) {
            orderItem.initializeOrderItem(super.getId(), new OrderItemId(itemId++));
        }
    }

    private Order(Builder builder) {
        super.setId(builder.orderId);
        customerId = builder.customerId;
        restaurantId = builder.restaurantId;
        deliveryAddress = builder.deliveryAddress;
        price = builder.price;
        deliveryFee = builder.deliveryFee;
        items = builder.items;
        trackingId = builder.trackingId;
        orderStatus = builder.orderStatus;
        createdAt = builder.createdAt;
        deliveredAt = builder.deliveredAt;
        isReviewed = builder.isReviewed;
        cancelReason = builder.cancelReason;
        materialAction = builder.materialAction;
        internalNote = builder.internalNote;
        version = builder.version;
    }

    public void markAsReviewed() {
        this.isReviewed = true;
    }

    public java.time.ZonedDateTime getCreatedAt() {
        return createdAt;
    }

    public java.time.ZonedDateTime getDeliveredAt() {
        return deliveredAt;
    }

    public boolean isReviewed() {
        return isReviewed;
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

    public Money getDeliveryFee() {
        return deliveryFee;
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

    public CancelReason getCancelReason() {
        return cancelReason;
    }

    public MaterialAction getMaterialAction() {
        return materialAction;
    }

    public String getInternalNote() {
        return internalNote;
    }

    public Integer getVersion() {
        return version;
    }

    public List<String> getFailureMessages() {
        List<String> list = new ArrayList<>();
        if (cancelReason != null) {
            list.add(cancelReason.name());
        }
        if (internalNote != null && !internalNote.isBlank()) {
            list.add(internalNote);
        }
        return list;
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
        private Money deliveryFee;
        private List<OrderItem> items;
        private TrackingId trackingId;
        private OrderStatus orderStatus;
        private java.time.ZonedDateTime createdAt;
        private java.time.ZonedDateTime deliveredAt;
        private boolean isReviewed;
        private CancelReason cancelReason;
        private MaterialAction materialAction;
        private String internalNote;
        private Integer version;

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

        public Builder deliveryFee(Money val) {
            deliveryFee = val;
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

        public Builder createdAt(java.time.ZonedDateTime val) {
            createdAt = val;
            return this;
        }

        public Builder deliveredAt(java.time.ZonedDateTime val) {
            deliveredAt = val;
            return this;
        }

        public Builder isReviewed(boolean val) {
            isReviewed = val;
            return this;
        }

        public Builder cancelReason(CancelReason val) {
            cancelReason = val;
            return this;
        }

        public Builder materialAction(MaterialAction val) {
            materialAction = val;
            return this;
        }

        public Builder internalNote(String val) {
            internalNote = val;
            return this;
        }

        public Builder version(Integer val) {
            version = val;
            return this;
        }

        public Builder failureMessages(List<String> val) {
            // Backward compatibility
            return this;
        }

        public Order build() {
            return new Order(this);
        }
    }
}
