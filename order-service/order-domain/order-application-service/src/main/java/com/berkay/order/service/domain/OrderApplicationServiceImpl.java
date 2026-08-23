package com.berkay.order.service.domain;

import com.berkay.order.service.domain.dto.create.CreateOrderCommand;
import com.berkay.order.service.domain.dto.create.CreateOrderResponse;
import com.berkay.order.service.domain.dto.query.PaidOrderResponse;
import com.berkay.order.service.domain.dto.track.TrackOrderQuery;
import com.berkay.order.service.domain.dto.track.TrackOrderResponse;
import com.berkay.order.service.domain.ports.input.service.OrderApplicationService;
import java.util.List;
import java.util.UUID;
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
    
    private final RestaurantPaidOrderQueryHandler restaurantPaidOrderQueryHandler;

    public OrderApplicationServiceImpl(OrderCreateCommandHandler orderCreateCommandHandler, OrderTrackCommandHandler orderTrackCommandHandler, CustomerOrderQueryHandler customerOrderQueryHandler, RestaurantPaidOrderQueryHandler restaurantPaidOrderQueryHandler) {
        this.orderCreateCommandHandler = orderCreateCommandHandler;
        this.orderTrackCommandHandler = orderTrackCommandHandler;
        this.customerOrderQueryHandler = customerOrderQueryHandler;
        this.restaurantPaidOrderQueryHandler = restaurantPaidOrderQueryHandler;
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
    public com.berkay.order.service.domain.dto.read.GetOrdersResponse getOrders(com.berkay.order.service.domain.dto.read.GetOrdersQuery getOrdersQuery) {
        return customerOrderQueryHandler.getOrders(getOrdersQuery);
    }
    
    @Override
    public List<PaidOrderResponse> getPaidOrders(UUID restaurantId) {
        return restaurantPaidOrderQueryHandler.getPaidOrders(restaurantId);
    }
}
