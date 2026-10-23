package com.berkay.payment.service.domain.entity;

import com.berkay.domain.entity.AggregateRoot;
import com.berkay.domain.valueobject.CustomerId;
import com.berkay.domain.valueobject.Money;
import com.berkay.domain.valueobject.OrderId;
import com.berkay.domain.valueobject.RestaurantId;
import com.berkay.payment.service.domain.exception.PaymentDomainException;
import com.berkay.payment.service.domain.valueobject.OrderEscrowId;
import com.berkay.payment.service.domain.valueobject.OrderEscrowStatus;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.UUID;

import static com.berkay.domain.DomainConstants.UTC;

public class OrderEscrow extends AggregateRoot<OrderEscrowId> {

    private final OrderId orderId;
    private final RestaurantId restaurantId;
    private final CustomerId customerId;
    private final Money amount;

    private OrderEscrowStatus status;
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;
    private final Integer version;

    public void initializeEscrow() {
        setId(new OrderEscrowId(UUID.randomUUID()));
        status = OrderEscrowStatus.HOLDING;
        createdAt = ZonedDateTime.now(ZoneId.of(UTC));
        updatedAt = createdAt;
    }

    public void release() {
        if (status != OrderEscrowStatus.HOLDING) {
            throw new PaymentDomainException("Order escrow is not in HOLDING status, current status: " + status);
        }
        status = OrderEscrowStatus.RELEASED;
        updatedAt = ZonedDateTime.now(ZoneId.of(UTC));
    }

    public void refund() {
        if (status != OrderEscrowStatus.HOLDING) {
            throw new PaymentDomainException("Order escrow is not in HOLDING status, current status: " + status);
        }
        status = OrderEscrowStatus.REFUNDED;
        updatedAt = ZonedDateTime.now(ZoneId.of(UTC));
    }

    private OrderEscrow(Builder builder) {
        setId(builder.orderEscrowId);
        orderId = builder.orderId;
        restaurantId = builder.restaurantId;
        customerId = builder.customerId;
        amount = builder.amount;
        status = builder.status;
        createdAt = builder.createdAt;
        updatedAt = builder.updatedAt;
        version = builder.version;
    }

    public static Builder builder() {
        return new Builder();
    }

    public OrderId getOrderId() {
        return orderId;
    }

    public RestaurantId getRestaurantId() {
        return restaurantId;
    }

    public CustomerId getCustomerId() {
        return customerId;
    }

    public Money getAmount() {
        return amount;
    }

    public OrderEscrowStatus getStatus() {
        return status;
    }

    public ZonedDateTime getCreatedAt() {
        return createdAt;
    }

    public ZonedDateTime getUpdatedAt() {
        return updatedAt;
    }

    public Integer getVersion() {
        return version;
    }

    public static final class Builder {
        private OrderEscrowId orderEscrowId;
        private OrderId orderId;
        private RestaurantId restaurantId;
        private CustomerId customerId;
        private Money amount;
        private OrderEscrowStatus status;
        private ZonedDateTime createdAt;
        private ZonedDateTime updatedAt;
        private Integer version;

        private Builder() {
        }

        public Builder orderEscrowId(OrderEscrowId val) {
            orderEscrowId = val;
            return this;
        }

        public Builder orderId(OrderId val) {
            orderId = val;
            return this;
        }

        public Builder restaurantId(RestaurantId val) {
            restaurantId = val;
            return this;
        }

        public Builder customerId(CustomerId val) {
            customerId = val;
            return this;
        }

        public Builder amount(Money val) {
            amount = val;
            return this;
        }

        public Builder status(OrderEscrowStatus val) {
            status = val;
            return this;
        }

        public Builder createdAt(ZonedDateTime val) {
            createdAt = val;
            return this;
        }

        public Builder updatedAt(ZonedDateTime val) {
            updatedAt = val;
            return this;
        }

        public Builder version(Integer val) {
            version = val;
            return this;
        }

        public OrderEscrow build() {
            return new OrderEscrow(this);
        }
    }
}
