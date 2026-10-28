package com.berkay.order.service.dataaccess.review.mapper;

import com.berkay.domain.valueobject.CustomerId;
import com.berkay.domain.valueobject.OrderId;
import com.berkay.domain.valueobject.RestaurantId;
import com.berkay.order.service.dataaccess.review.entity.OrderReviewEntity;
import com.berkay.order.service.domain.entity.OrderReview;
import com.berkay.order.service.domain.valueobject.OrderReviewId;
import org.springframework.stereotype.Component;

@Component
public class OrderReviewDataAccessMapper {

    public OrderReviewEntity orderReviewToOrderReviewEntity(OrderReview orderReview) {
        if (orderReview == null) {
            return null;
        }
        return OrderReviewEntity.builder()
                .id(orderReview.getId().getValue())
                .orderId(orderReview.getOrderId().getValue())
                .customerId(orderReview.getCustomerId().getValue())
                .restaurantId(orderReview.getRestaurantId().getValue())
                .rating(orderReview.getRating())
                .comment(orderReview.getComment())
                .customerNameMasked(orderReview.getCustomerNameMasked())
                .restaurantReply(orderReview.getRestaurantReply())
                .restaurantRepliedAt(orderReview.getRestaurantRepliedAt())
                .isActive(orderReview.isActive())
                .deactivationReason(orderReview.getDeactivationReason())
                .deactivatedAt(orderReview.getDeactivatedAt())
                .deactivatedBy(orderReview.getDeactivatedBy())
                .createdAt(orderReview.getCreatedAt())
                .updatedAt(orderReview.getUpdatedAt())
                .build();
    }

    public OrderReview orderReviewEntityToOrderReview(OrderReviewEntity entity) {
        if (entity == null) {
            return null;
        }
        return OrderReview.builder()
                .id(new OrderReviewId(entity.getId()))
                .orderId(new OrderId(entity.getOrderId()))
                .customerId(new CustomerId(entity.getCustomerId()))
                .restaurantId(new RestaurantId(entity.getRestaurantId()))
                .rating(entity.getRating())
                .comment(entity.getComment())
                .customerNameMasked(entity.getCustomerNameMasked())
                .restaurantReply(entity.getRestaurantReply())
                .restaurantRepliedAt(entity.getRestaurantRepliedAt())
                .active(entity.isActive())
                .deactivationReason(entity.getDeactivationReason())
                .deactivatedAt(entity.getDeactivatedAt())
                .deactivatedBy(entity.getDeactivatedBy())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
