package com.berkay.order.service.domain.mapper;

import com.berkay.domain.valueobject.OrderId;
import com.berkay.order.service.domain.dto.review.*;
import com.berkay.order.service.domain.entity.Customer;
import com.berkay.order.service.domain.entity.Order;
import com.berkay.order.service.domain.entity.OrderReview;
import com.berkay.order.service.domain.valueobject.OrderReviewId;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class OrderReviewDataMapper {

    public String maskCustomerName(String firstName, String lastName) {
        String maskedFirst = maskPart(firstName);
        String maskedLast = maskPart(lastName);
        if (maskedFirst.isEmpty() && maskedLast.isEmpty()) {
            return "Müşteri";
        }
        return (maskedFirst + " " + maskedLast).trim();
    }

    private String maskPart(String part) {
        if (part == null || part.trim().isEmpty()) {
            return "";
        }
        String clean = part.trim();
        return clean.substring(0, 1).toUpperCase() + "***";
    }

    public OrderReview createOrderReviewCommandToOrderReview(CreateOrderReviewCommand command,
                                                             Order order,
                                                             Customer customer) {
        String firstName = customer != null ? customer.getFirstName() : null;
        String lastName = customer != null ? customer.getLastName() : null;
        String maskedName = maskCustomerName(firstName, lastName);

        return OrderReview.builder()
                .id(new OrderReviewId(UUID.randomUUID()))
                .orderId(order.getId())
                .customerId(order.getCustomerId())
                .restaurantId(order.getRestaurantId())
                .rating(command.getRating())
                .comment(command.getComment() != null ? command.getComment().trim() : null)
                .customerNameMasked(maskedName)
                .build();
    }

    public CreateOrderReviewResponse orderReviewToCreateOrderReviewResponse(OrderReview orderReview, String message) {
        return CreateOrderReviewResponse.builder()
                .reviewId(orderReview.getId().getValue())
                .message(message)
                .build();
    }

    public ReplyOrderReviewResponse orderReviewToReplyOrderReviewResponse(OrderReview orderReview, String message) {
        return ReplyOrderReviewResponse.builder()
                .reviewId(orderReview.getId().getValue())
                .message(message)
                .build();
    }

    public DeactivateOrderReviewResponse orderReviewToDeactivateOrderReviewResponse(OrderReview orderReview, String message) {
        return DeactivateOrderReviewResponse.builder()
                .reviewId(orderReview.getId().getValue())
                .message(message)
                .build();
    }

    public OrderReviewResponse orderReviewToOrderReviewResponse(OrderReview orderReview) {
        return OrderReviewResponse.builder()
                .reviewId(orderReview.getId().getValue())
                .orderId(orderReview.getOrderId().getValue())
                .restaurantId(orderReview.getRestaurantId().getValue())
                .rating(orderReview.getRating())
                .comment(orderReview.getComment())
                .customerNameMasked(orderReview.getCustomerNameMasked())
                .restaurantReply(orderReview.getRestaurantReply())
                .restaurantRepliedAt(orderReview.getRestaurantRepliedAt())
                .createdAt(orderReview.getCreatedAt())
                .canReply(orderReview.canReply())
                .build();
    }

    public AdminOrderReviewResponse orderReviewToAdminOrderReviewResponse(OrderReview orderReview) {
        return AdminOrderReviewResponse.builder()
                .reviewId(orderReview.getId().getValue())
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

    public CustomerOrderReviewResponse orderReviewToCustomerOrderReviewResponse(OrderReview orderReview) {
        return CustomerOrderReviewResponse.builder()
                .reviewId(orderReview.getId().getValue())
                .orderId(orderReview.getOrderId().getValue())
                .restaurantId(orderReview.getRestaurantId().getValue())
                .rating(orderReview.getRating())
                .comment(orderReview.getComment())
                .customerNameMasked(orderReview.getCustomerNameMasked())
                .restaurantReply(orderReview.getRestaurantReply())
                .restaurantRepliedAt(orderReview.getRestaurantRepliedAt())
                .createdAt(orderReview.getCreatedAt())
                .build();
    }

    public GetRestaurantReviewsResponse orderReviewPageResultToGetRestaurantReviewsResponse(OrderReviewPageResult pageResult,
                                                                                            long repliedCount,
                                                                                            long pendingReplyCount) {
        List<OrderReviewResponse> responses = pageResult.getReviews().stream()
                .map(this::orderReviewToOrderReviewResponse)
                .collect(Collectors.toList());
        return GetRestaurantReviewsResponse.builder()
                .reviews(responses)
                .page(pageResult.getPage())
                .size(pageResult.getSize())
                .totalElements(pageResult.getTotalElements())
                .totalPages(pageResult.getTotalPages())
                .isLast(pageResult.isLast())
                .repliedCount(repliedCount)
                .pendingReplyCount(pendingReplyCount)
                .build();
    }

    public GetRestaurantReviewsResponse orderReviewPageResultToGetRestaurantReviewsResponse(OrderReviewPageResult pageResult) {
        return orderReviewPageResultToGetRestaurantReviewsResponse(pageResult, 0L, 0L);
    }

    public GetAdminRestaurantReviewsResponse orderReviewPageResultToGetAdminRestaurantReviewsResponse(OrderReviewPageResult pageResult) {
        List<AdminOrderReviewResponse> responses = pageResult.getReviews().stream()
                .map(this::orderReviewToAdminOrderReviewResponse)
                .collect(Collectors.toList());
        return GetAdminRestaurantReviewsResponse.builder()
                .reviews(responses)
                .page(pageResult.getPage())
                .size(pageResult.getSize())
                .totalElements(pageResult.getTotalElements())
                .totalPages(pageResult.getTotalPages())
                .isLast(pageResult.isLast())
                .build();
    }
}
