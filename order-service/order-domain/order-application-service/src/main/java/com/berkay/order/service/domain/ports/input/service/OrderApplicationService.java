package com.berkay.order.service.domain.ports.input.service;

import com.berkay.order.service.domain.dto.create.CreateOrderCommand;
import com.berkay.order.service.domain.dto.create.CreateOrderResponse;
import com.berkay.order.service.domain.dto.operation.*;
import com.berkay.order.service.domain.dto.read.CustomerOrderDetailQuery;
import com.berkay.order.service.domain.dto.read.CustomerOrderDetailResponse;
import com.berkay.order.service.domain.dto.read.GetOrdersQuery;
import com.berkay.order.service.domain.dto.read.GetOrdersResponse;
import com.berkay.order.service.domain.dto.restaurant.*;
import com.berkay.order.service.domain.dto.track.TrackOrderQuery;
import com.berkay.order.service.domain.dto.track.TrackOrderResponse;
import jakarta.validation.Valid;

public interface OrderApplicationService {

    CreateOrderResponse createOrder(@Valid CreateOrderCommand createOrderCommand);

    TrackOrderResponse trackOrder(@Valid TrackOrderQuery trackOrderQuery);

    GetOrdersResponse getOrders(@Valid GetOrdersQuery getOrdersQuery);

    CustomerOrderDetailResponse getCustomerOrderDetail(@Valid CustomerOrderDetailQuery query);

    RestaurantOrderResponse approveOrder(@Valid ApproveOrderCommand command);

    RestaurantOrderResponse startPreparingOrder(@Valid StartPreparingOrderCommand command);

    RestaurantOrderResponse readyForPickupOrder(@Valid ReadyForPickupOrderCommand command);

    RestaurantOrderResponse startDeliveryOrder(@Valid StartDeliveryOrderCommand command);

    RestaurantOrderResponse deliverOrder(@Valid DeliverOrderCommand command);

    RestaurantOrderResponse cancelOrder(@Valid CancelOrderCommand command);

    RestaurantOrderResponse forceCancelOrder(@Valid ForceCancelOrderCommand command);

    RestaurantOrdersPageResponse getRestaurantOrders(@Valid RestaurantOrdersQuery query);

    RestaurantOrderResponse getOrderDetail(@Valid OrderDetailQuery query);

    RestaurantOrderResponse getPlatformOrder(@Valid PlatformOrderQuery query);

    RestaurantOrdersSummaryResponse getRestaurantOrdersSummary(java.util.UUID restaurantId);

    com.berkay.order.service.domain.dto.review.CreateOrderReviewResponse createReview(
            @Valid com.berkay.order.service.domain.dto.review.CreateOrderReviewCommand command);

    com.berkay.order.service.domain.dto.review.CustomerOrderReviewResponse getCustomerOrderReview(java.util.UUID orderId);

    com.berkay.order.service.domain.dto.review.ReplyOrderReviewResponse replyReview(
            @Valid com.berkay.order.service.domain.dto.review.ReplyOrderReviewCommand command);

    com.berkay.order.service.domain.dto.review.DeactivateOrderReviewResponse deactivateReview(
            @Valid com.berkay.order.service.domain.dto.review.DeactivateOrderReviewCommand command);

    com.berkay.order.service.domain.dto.review.GetRestaurantReviewsResponse getRestaurantReviews(
            @Valid com.berkay.order.service.domain.dto.review.GetRestaurantReviewsQuery query);

    com.berkay.order.service.domain.dto.review.GetAdminRestaurantReviewsResponse getAdminRestaurantReviews(
            @Valid com.berkay.order.service.domain.dto.review.GetAdminRestaurantReviewsQuery query);
}
