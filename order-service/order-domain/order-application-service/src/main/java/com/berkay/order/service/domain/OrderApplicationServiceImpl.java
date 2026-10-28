package com.berkay.order.service.domain;

import com.berkay.order.service.domain.dto.create.CreateOrderCommand;
import com.berkay.order.service.domain.dto.create.CreateOrderResponse;
import com.berkay.order.service.domain.dto.operation.*;
import com.berkay.order.service.domain.dto.read.GetOrdersQuery;
import com.berkay.order.service.domain.dto.read.GetOrdersResponse;
import com.berkay.order.service.domain.dto.restaurant.*;
import com.berkay.order.service.domain.dto.track.TrackOrderQuery;
import com.berkay.order.service.domain.dto.track.TrackOrderResponse;
import com.berkay.order.service.domain.ports.input.service.OrderApplicationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Slf4j
@Validated
@Service
class OrderApplicationServiceImpl implements OrderApplicationService {

    private final OrderCreateCommandHandler orderCreateCommandHandler;
    private final OrderTrackCommandHandler orderTrackCommandHandler;
    private final CustomerOrderQueryHandler customerOrderQueryHandler;
    private final OrderApproveCommandHandler orderApproveCommandHandler;
    private final OrderStartPreparingCommandHandler orderStartPreparingCommandHandler;
    private final OrderReadyForPickupCommandHandler orderReadyForPickupCommandHandler;
    private final OrderStartDeliveryCommandHandler orderStartDeliveryCommandHandler;
    private final OrderDeliverCommandHandler orderDeliverCommandHandler;
    private final OrderCancelCommandHandler orderCancelCommandHandler;
    private final OrderForceCancelCommandHandler orderForceCancelCommandHandler;
    private final RestaurantOrdersQueryHandler restaurantOrdersQueryHandler;
    private final OrderDetailQueryHandler orderDetailQueryHandler;
    private final PlatformOrderQueryHandler platformOrderQueryHandler;
    private final RestaurantOrdersSummaryQueryHandler restaurantOrdersSummaryQueryHandler;
    private final com.berkay.order.service.domain.review.CreateOrderReviewCommandHandler createOrderReviewCommandHandler;
    private final com.berkay.order.service.domain.review.GetCustomerOrderReviewQueryHandler getCustomerOrderReviewQueryHandler;
    private final com.berkay.order.service.domain.review.ReplyOrderReviewCommandHandler replyOrderReviewCommandHandler;
    private final com.berkay.order.service.domain.review.DeactivateOrderReviewCommandHandler deactivateOrderReviewCommandHandler;
    private final com.berkay.order.service.domain.review.GetRestaurantReviewsQueryHandler getRestaurantReviewsQueryHandler;
    private final com.berkay.order.service.domain.review.GetAdminRestaurantReviewsQueryHandler getAdminRestaurantReviewsQueryHandler;

    public OrderApplicationServiceImpl(OrderCreateCommandHandler orderCreateCommandHandler,
                                      OrderTrackCommandHandler orderTrackCommandHandler,
                                      CustomerOrderQueryHandler customerOrderQueryHandler,
                                      OrderApproveCommandHandler orderApproveCommandHandler,
                                      OrderStartPreparingCommandHandler orderStartPreparingCommandHandler,
                                      OrderReadyForPickupCommandHandler orderReadyForPickupCommandHandler,
                                      OrderStartDeliveryCommandHandler orderStartDeliveryCommandHandler,
                                      OrderDeliverCommandHandler orderDeliverCommandHandler,
                                      OrderCancelCommandHandler orderCancelCommandHandler,
                                      OrderForceCancelCommandHandler orderForceCancelCommandHandler,
                                      RestaurantOrdersQueryHandler restaurantOrdersQueryHandler,
                                      OrderDetailQueryHandler orderDetailQueryHandler,
                                      PlatformOrderQueryHandler platformOrderQueryHandler,
                                      RestaurantOrdersSummaryQueryHandler restaurantOrdersSummaryQueryHandler,
                                      com.berkay.order.service.domain.review.CreateOrderReviewCommandHandler createOrderReviewCommandHandler,
                                      com.berkay.order.service.domain.review.GetCustomerOrderReviewQueryHandler getCustomerOrderReviewQueryHandler,
                                      com.berkay.order.service.domain.review.ReplyOrderReviewCommandHandler replyOrderReviewCommandHandler,
                                      com.berkay.order.service.domain.review.DeactivateOrderReviewCommandHandler deactivateOrderReviewCommandHandler,
                                      com.berkay.order.service.domain.review.GetRestaurantReviewsQueryHandler getRestaurantReviewsQueryHandler,
                                      com.berkay.order.service.domain.review.GetAdminRestaurantReviewsQueryHandler getAdminRestaurantReviewsQueryHandler) {
        this.orderCreateCommandHandler = orderCreateCommandHandler;
        this.orderTrackCommandHandler = orderTrackCommandHandler;
        this.customerOrderQueryHandler = customerOrderQueryHandler;
        this.orderApproveCommandHandler = orderApproveCommandHandler;
        this.orderStartPreparingCommandHandler = orderStartPreparingCommandHandler;
        this.orderReadyForPickupCommandHandler = orderReadyForPickupCommandHandler;
        this.orderStartDeliveryCommandHandler = orderStartDeliveryCommandHandler;
        this.orderDeliverCommandHandler = orderDeliverCommandHandler;
        this.orderCancelCommandHandler = orderCancelCommandHandler;
        this.orderForceCancelCommandHandler = orderForceCancelCommandHandler;
        this.restaurantOrdersQueryHandler = restaurantOrdersQueryHandler;
        this.orderDetailQueryHandler = orderDetailQueryHandler;
        this.platformOrderQueryHandler = platformOrderQueryHandler;
        this.restaurantOrdersSummaryQueryHandler = restaurantOrdersSummaryQueryHandler;
        this.createOrderReviewCommandHandler = createOrderReviewCommandHandler;
        this.getCustomerOrderReviewQueryHandler = getCustomerOrderReviewQueryHandler;
        this.replyOrderReviewCommandHandler = replyOrderReviewCommandHandler;
        this.deactivateOrderReviewCommandHandler = deactivateOrderReviewCommandHandler;
        this.getRestaurantReviewsQueryHandler = getRestaurantReviewsQueryHandler;
        this.getAdminRestaurantReviewsQueryHandler = getAdminRestaurantReviewsQueryHandler;
    }

    @Override
    public CreateOrderResponse createOrder(CreateOrderCommand createOrderCommand) {
        return orderCreateCommandHandler.createOrder(createOrderCommand);
    }

    @Override
    public TrackOrderResponse trackOrder(TrackOrderQuery trackOrderQuery) {
        return orderTrackCommandHandler.trackOrder(trackOrderQuery);
    }

    @Override
    public GetOrdersResponse getOrders(GetOrdersQuery getOrdersQuery) {
        return customerOrderQueryHandler.getOrders(getOrdersQuery);
    }

    @Override
    public com.berkay.order.service.domain.dto.read.CustomerOrderDetailResponse getCustomerOrderDetail(
            com.berkay.order.service.domain.dto.read.CustomerOrderDetailQuery query) {
        return customerOrderQueryHandler.getOrderDetail(query);
    }

    @Override
    public RestaurantOrderResponse approveOrder(ApproveOrderCommand command) {
        return orderApproveCommandHandler.approveOrder(command);
    }

    @Override
    public RestaurantOrderResponse startPreparingOrder(StartPreparingOrderCommand command) {
        return orderStartPreparingCommandHandler.startPreparingOrder(command);
    }

    @Override
    public RestaurantOrderResponse readyForPickupOrder(ReadyForPickupOrderCommand command) {
        return orderReadyForPickupCommandHandler.readyForPickupOrder(command);
    }

    @Override
    public RestaurantOrderResponse startDeliveryOrder(StartDeliveryOrderCommand command) {
        return orderStartDeliveryCommandHandler.startDeliveryOrder(command);
    }

    @Override
    public RestaurantOrderResponse deliverOrder(DeliverOrderCommand command) {
        return orderDeliverCommandHandler.deliverOrder(command);
    }

    @Override
    public RestaurantOrderResponse cancelOrder(CancelOrderCommand command) {
        return orderCancelCommandHandler.cancelOrder(command);
    }

    @Override
    public RestaurantOrderResponse forceCancelOrder(ForceCancelOrderCommand command) {
        return orderForceCancelCommandHandler.forceCancelOrder(command);
    }

    @Override
    public RestaurantOrdersPageResponse getRestaurantOrders(RestaurantOrdersQuery query) {
        return restaurantOrdersQueryHandler.getRestaurantOrders(query);
    }

    @Override
    public RestaurantOrderResponse getOrderDetail(OrderDetailQuery query) {
        return orderDetailQueryHandler.getOrderDetail(query);
    }

    @Override
    public RestaurantOrderResponse getPlatformOrder(PlatformOrderQuery query) {
        return platformOrderQueryHandler.getPlatformOrder(query);
    }

    @Override
    public RestaurantOrdersSummaryResponse getRestaurantOrdersSummary(java.util.UUID restaurantId) {
        return restaurantOrdersSummaryQueryHandler.getRestaurantOrdersSummary(restaurantId);
    }

    @Override
    public com.berkay.order.service.domain.dto.review.CreateOrderReviewResponse createReview(
            com.berkay.order.service.domain.dto.review.CreateOrderReviewCommand command) {
        return createOrderReviewCommandHandler.createReview(command);
    }

    @Override
    public com.berkay.order.service.domain.dto.review.CustomerOrderReviewResponse getCustomerOrderReview(java.util.UUID orderId) {
        return getCustomerOrderReviewQueryHandler.getReview(orderId);
    }

    @Override
    public com.berkay.order.service.domain.dto.review.ReplyOrderReviewResponse replyReview(
            com.berkay.order.service.domain.dto.review.ReplyOrderReviewCommand command) {
        return replyOrderReviewCommandHandler.replyReview(command);
    }

    @Override
    public com.berkay.order.service.domain.dto.review.DeactivateOrderReviewResponse deactivateReview(
            com.berkay.order.service.domain.dto.review.DeactivateOrderReviewCommand command) {
        return deactivateOrderReviewCommandHandler.deactivateReview(command);
    }

    @Override
    public com.berkay.order.service.domain.dto.review.GetRestaurantReviewsResponse getRestaurantReviews(
            com.berkay.order.service.domain.dto.review.GetRestaurantReviewsQuery query) {
        return getRestaurantReviewsQueryHandler.getRestaurantReviews(query);
    }

    @Override
    public com.berkay.order.service.domain.dto.review.GetAdminRestaurantReviewsResponse getAdminRestaurantReviews(
            com.berkay.order.service.domain.dto.review.GetAdminRestaurantReviewsQuery query) {
        return getAdminRestaurantReviewsQueryHandler.getAdminRestaurantReviews(query);
    }
}
