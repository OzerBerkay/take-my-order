package com.berkay.order.service.application.rest;

import com.berkay.order.service.application.rest.dto.review.CreateOrderReviewRequest;
import com.berkay.order.service.application.rest.dto.review.DeactivateOrderReviewRequest;
import com.berkay.order.service.application.rest.dto.review.ReplyOrderReviewRequest;
import com.berkay.order.service.domain.dto.review.*;
import com.berkay.order.service.domain.ports.input.service.OrderApplicationService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping(value = "/orders", produces = MediaType.APPLICATION_JSON_VALUE)
public class OrderReviewController {

    private final OrderApplicationService orderApplicationService;

    public OrderReviewController(OrderApplicationService orderApplicationService) {
        this.orderApplicationService = orderApplicationService;
    }

    @PreAuthorize("@orderAuthService.isOrderCustomer(authentication, #orderId)")
    @PostMapping("/{orderId}/reviews")
    public ResponseEntity<CreateOrderReviewResponse> createReview(
            @PathVariable("orderId") UUID orderId,
            @RequestBody @Valid CreateOrderReviewRequest request) {
        log.info("Creating review for order: {}", orderId);

        CreateOrderReviewCommand command = CreateOrderReviewCommand.builder()
                .orderId(orderId)
                .rating(request.getRating())
                .comment(request.getComment())
                .build();

        CreateOrderReviewResponse response = orderApplicationService.createReview(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PreAuthorize("@orderAuthService.isOrderCustomer(authentication, #orderId)")
    @GetMapping("/{orderId}/review")
    public ResponseEntity<CustomerOrderReviewResponse> getCustomerOrderReview(
            @PathVariable("orderId") UUID orderId) {
        log.info("Fetching customer review for order: {}", orderId);
        CustomerOrderReviewResponse response = orderApplicationService.getCustomerOrderReview(orderId);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("@orderAuthService.hasPermissionForRestaurant(authentication, 'can_reply_restaurant_review', #restaurantId)")
    @PostMapping("/restaurants/{restaurantId}/reviews/{reviewId}/reply")
    public ResponseEntity<ReplyOrderReviewResponse> replyReview(
            @PathVariable("restaurantId") UUID restaurantId,
            @PathVariable("reviewId") UUID reviewId,
            @RequestBody @Valid ReplyOrderReviewRequest request) {
        log.info("Restaurant {} replying to review: {}", restaurantId, reviewId);

        ReplyOrderReviewCommand command = ReplyOrderReviewCommand.builder()
                .restaurantId(restaurantId)
                .reviewId(reviewId)
                .reply(request.getReply())
                .build();

        ReplyOrderReviewResponse response = orderApplicationService.replyReview(command);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("@orderAuthService.hasPermission(authentication, 'can_delete_restaurant_review')")
    @DeleteMapping("/restaurants/{restaurantId}/reviews/{reviewId}")
    public ResponseEntity<DeactivateOrderReviewResponse> deactivateReview(
            @PathVariable("restaurantId") UUID restaurantId,
            @PathVariable("reviewId") UUID reviewId,
            @AuthenticationPrincipal UUID moderatorId,
            @RequestBody @Valid DeactivateOrderReviewRequest request) {
        log.info("Moderator {} deactivating review {} for restaurant {}", moderatorId, reviewId, restaurantId);

        DeactivateOrderReviewCommand command = DeactivateOrderReviewCommand.builder()
                .restaurantId(restaurantId)
                .reviewId(reviewId)
                .moderatorId(moderatorId)
                .reason(request.getReason())
                .build();

        DeactivateOrderReviewResponse response = orderApplicationService.deactivateReview(command);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/restaurants/{restaurantId}/reviews")
    public ResponseEntity<GetRestaurantReviewsResponse> getRestaurantReviews(
            @PathVariable("restaurantId") UUID restaurantId,
            @RequestParam(required = false, defaultValue = "ALL") String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("Fetching public reviews for restaurant: {}, status: {}, page: {}, size: {}", restaurantId, status, page, size);

        GetRestaurantReviewsQuery query = GetRestaurantReviewsQuery.builder()
                .restaurantId(restaurantId)
                .status(status)
                .page(page)
                .size(size)
                .build();

        GetRestaurantReviewsResponse response = orderApplicationService.getRestaurantReviews(query);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("@orderAuthService.hasPermission(authentication, 'can_delete_restaurant_review')")
    @GetMapping("/admin/restaurants/{restaurantId}/reviews")
    public ResponseEntity<GetAdminRestaurantReviewsResponse> getAdminRestaurantReviews(
            @PathVariable("restaurantId") UUID restaurantId,
            @RequestParam(required = false, defaultValue = "ALL") String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("Admin fetching reviews for restaurant: {}, status: {}, page: {}, size: {}",
                restaurantId, status, page, size);

        GetAdminRestaurantReviewsQuery query = GetAdminRestaurantReviewsQuery.builder()
                .restaurantId(restaurantId)
                .status(status)
                .page(page)
                .size(size)
                .build();

        GetAdminRestaurantReviewsResponse response = orderApplicationService.getAdminRestaurantReviews(query);
        return ResponseEntity.ok(response);
    }
}
