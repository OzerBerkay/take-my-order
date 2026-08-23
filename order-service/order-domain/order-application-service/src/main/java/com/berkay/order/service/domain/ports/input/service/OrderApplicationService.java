package com.berkay.order.service.domain.ports.input.service;

import com.berkay.order.service.domain.dto.create.CreateOrderCommand;
import com.berkay.order.service.domain.dto.create.CreateOrderResponse;
import com.berkay.order.service.domain.dto.query.PaidOrderResponse;
import com.berkay.order.service.domain.dto.read.GetOrdersQuery;
import com.berkay.order.service.domain.dto.read.GetOrdersResponse;
import com.berkay.order.service.domain.dto.track.TrackOrderQuery;
import com.berkay.order.service.domain.dto.track.TrackOrderResponse;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;

public interface OrderApplicationService {

    // @Valid is for the validation of some fields in DTO's like @NotNull or @Max
    CreateOrderResponse createOrder(@Valid CreateOrderCommand createOrderCommand);

    TrackOrderResponse trackOrder(@Valid TrackOrderQuery trackOrderQuery);

    GetOrdersResponse getOrders(@Valid GetOrdersQuery getOrdersQuery);

    List<PaidOrderResponse> getPaidOrders(UUID restaurantId);
}
