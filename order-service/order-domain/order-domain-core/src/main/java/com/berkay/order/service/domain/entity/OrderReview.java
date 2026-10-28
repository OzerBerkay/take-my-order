package com.berkay.order.service.domain.entity;

import com.berkay.domain.entity.AggregateRoot;
import com.berkay.domain.valueobject.CustomerId;
import com.berkay.domain.valueobject.OrderId;
import com.berkay.domain.valueobject.RestaurantId;
import com.berkay.order.service.domain.exception.OrderDomainException;
import com.berkay.order.service.domain.exception.ReviewAlreadyDeactivatedException;
import com.berkay.order.service.domain.exception.ReviewTimeExpiredException;
import com.berkay.order.service.domain.valueobject.OrderReviewId;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.UUID;

public class OrderReview extends AggregateRoot<OrderReviewId> {
    private final OrderId orderId;
    private final CustomerId customerId;
    private final RestaurantId restaurantId;
    private final int rating;
    private final String comment;
    private final String customerNameMasked;

    private String restaurantReply;
    private ZonedDateTime restaurantRepliedAt;

    private boolean active;
    private String deactivationReason;
    private ZonedDateTime deactivatedAt;
    private UUID deactivatedBy;

    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;

    public void initializeReview() {
        if (getId() == null) {
            setId(new OrderReviewId(UUID.randomUUID()));
        }
        if (rating < 1 || rating > 5) {
            throw new OrderDomainException("INVALID_REVIEW_RATING", "Review rating must be between 1 and 5! Given: " + rating);
        }
        this.active = true;
        this.createdAt = ZonedDateTime.now(ZoneId.of("UTC"));
    }

    public void reply(String reply) {
        if (!this.active) {
            throw new OrderDomainException("REVIEW_INACTIVE", "Cannot reply to an inactive or deactivated review!");
        }
        if (this.restaurantReply != null) {
            throw new OrderDomainException("REVIEW_ALREADY_REPLIED", "Review has already been replied to! Multiple replies are not permitted.");
        }
        if (reply == null || reply.trim().isEmpty()) {
            throw new OrderDomainException("REPLY_CANNOT_BE_EMPTY", "Restaurant reply cannot be empty!");
        }
        if (this.createdAt != null && this.createdAt.plusDays(7).isBefore(ZonedDateTime.now(ZoneId.of("UTC")))) {
            throw new ReviewTimeExpiredException("The 7-day reply window for this review has expired!");
        }
        this.restaurantReply = reply.trim();
        this.restaurantRepliedAt = ZonedDateTime.now(ZoneId.of("UTC"));
        this.updatedAt = this.restaurantRepliedAt;
    }

    public boolean canReply() {
        return this.active && this.restaurantReply == null &&
                (this.createdAt == null || !this.createdAt.plusDays(7).isBefore(ZonedDateTime.now(ZoneId.of("UTC"))));
    }

    public void deactivate(String reason, UUID moderatorId) {
        if (!this.active) {
            throw new ReviewAlreadyDeactivatedException("Review is already deactivated! Deactivation is strictly irreversible.");
        }
        if (reason == null || reason.trim().isEmpty()) {
            throw new OrderDomainException("DEACTIVATION_REASON_CANNOT_BE_EMPTY", "Deactivation reason cannot be empty!");
        }
        this.active = false;
        this.deactivationReason = reason.trim();
        this.deactivatedBy = moderatorId;
        this.deactivatedAt = ZonedDateTime.now(ZoneId.of("UTC"));
        this.updatedAt = this.deactivatedAt;
    }

    private OrderReview(Builder builder) {
        super.setId(builder.id);
        orderId = builder.orderId;
        customerId = builder.customerId;
        restaurantId = builder.restaurantId;
        rating = builder.rating;
        comment = builder.comment;
        customerNameMasked = builder.customerNameMasked;
        restaurantReply = builder.restaurantReply;
        restaurantRepliedAt = builder.restaurantRepliedAt;
        active = builder.active;
        deactivationReason = builder.deactivationReason;
        deactivatedAt = builder.deactivatedAt;
        deactivatedBy = builder.deactivatedBy;
        createdAt = builder.createdAt;
        updatedAt = builder.updatedAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public OrderId getOrderId() {
        return orderId;
    }

    public CustomerId getCustomerId() {
        return customerId;
    }

    public RestaurantId getRestaurantId() {
        return restaurantId;
    }

    public int getRating() {
        return rating;
    }

    public String getComment() {
        return comment;
    }

    public String getCustomerNameMasked() {
        return customerNameMasked;
    }

    public String getRestaurantReply() {
        return restaurantReply;
    }

    public ZonedDateTime getRestaurantRepliedAt() {
        return restaurantRepliedAt;
    }

    public boolean isActive() {
        return active;
    }

    public String getDeactivationReason() {
        return deactivationReason;
    }

    public ZonedDateTime getDeactivatedAt() {
        return deactivatedAt;
    }

    public UUID getDeactivatedBy() {
        return deactivatedBy;
    }

    public ZonedDateTime getCreatedAt() {
        return createdAt;
    }

    public ZonedDateTime getUpdatedAt() {
        return updatedAt;
    }

    public static final class Builder {
        private OrderReviewId id;
        private OrderId orderId;
        private CustomerId customerId;
        private RestaurantId restaurantId;
        private int rating;
        private String comment;
        private String customerNameMasked;
        private String restaurantReply;
        private ZonedDateTime restaurantRepliedAt;
        private boolean active = true;
        private String deactivationReason;
        private ZonedDateTime deactivatedAt;
        private UUID deactivatedBy;
        private ZonedDateTime createdAt;
        private ZonedDateTime updatedAt;

        private Builder() {
        }

        public Builder id(OrderReviewId val) {
            id = val;
            return this;
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

        public Builder rating(int val) {
            rating = val;
            return this;
        }

        public Builder comment(String val) {
            comment = val;
            return this;
        }

        public Builder customerNameMasked(String val) {
            customerNameMasked = val;
            return this;
        }

        public Builder restaurantReply(String val) {
            restaurantReply = val;
            return this;
        }

        public Builder restaurantRepliedAt(ZonedDateTime val) {
            restaurantRepliedAt = val;
            return this;
        }

        public Builder active(boolean val) {
            active = val;
            return this;
        }

        public Builder deactivationReason(String val) {
            deactivationReason = val;
            return this;
        }

        public Builder deactivatedAt(ZonedDateTime val) {
            deactivatedAt = val;
            return this;
        }

        public Builder deactivatedBy(UUID val) {
            deactivatedBy = val;
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

        public OrderReview build() {
            return new OrderReview(this);
        }
    }
}
