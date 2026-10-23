package com.berkay.order.service.domain;

import com.berkay.domain.valueobject.CustomerId;
import com.berkay.domain.valueobject.OrderId;
import com.berkay.order.service.domain.dto.read.CustomerOrderDetailQuery;
import com.berkay.order.service.domain.dto.read.CustomerOrderDetailResponse;
import com.berkay.order.service.domain.dto.read.GetOrdersQuery;
import com.berkay.order.service.domain.dto.read.GetOrdersResponse;
import com.berkay.order.service.domain.dto.read.OrderItemSummaryDto;
import com.berkay.order.service.domain.dto.read.OrderSummary;
import com.berkay.order.service.domain.entity.Order;
import com.berkay.order.service.domain.exception.OrderNotFoundException;
import com.berkay.order.service.domain.mapper.OrderDataMapper;
import com.berkay.order.service.domain.ports.output.repository.OrderRepository;
import com.berkay.order.service.domain.ports.output.repository.RestaurantRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Component
public class CustomerOrderQueryHandler {

    private final OrderRepository orderRepository;
    private final RestaurantRepository restaurantRepository;
    private final OrderDataMapper orderDataMapper;

    public CustomerOrderQueryHandler(OrderRepository orderRepository,
                                    RestaurantRepository restaurantRepository,
                                    OrderDataMapper orderDataMapper) {
        this.orderRepository = orderRepository;
        this.restaurantRepository = restaurantRepository;
        this.orderDataMapper = orderDataMapper;
    }

    @Transactional(readOnly = true)
    public CustomerOrderDetailResponse getOrderDetail(CustomerOrderDetailQuery query) {
        Order order = orderRepository.findById(new OrderId(query.getOrderId()))
                .orElseThrow(() -> new OrderNotFoundException("Order with id " + query.getOrderId() + " could not be found!"));

        String restaurantName = restaurantRepository.findRestaurantByRestaurantId(order.getRestaurantId().getValue())
                .map(com.berkay.order.service.domain.entity.Restaurant::getName)
                .orElse(null);

        return orderDataMapper.orderToCustomerOrderDetailResponse(order, restaurantName);
    }

    @Transactional(readOnly = true)
    public GetOrdersResponse getOrders(GetOrdersQuery getOrdersQuery) {
        com.berkay.order.service.domain.dto.read.OrderFilterGroup filter =
                getOrdersQuery.getFilter() != null ? getOrdersQuery.getFilter() : com.berkay.order.service.domain.dto.read.OrderFilterGroup.ALL;

        CustomerId customerId = new CustomerId(getOrdersQuery.getCustomerId());

        com.berkay.order.service.domain.dto.read.OrderPageResult pageResult;
        if (filter == com.berkay.order.service.domain.dto.read.OrderFilterGroup.ALL) {
            pageResult = orderRepository.findByCustomerId(
                    customerId,
                    getOrdersQuery.getPage(),
                    getOrdersQuery.getSize()
            );
        } else {
            pageResult = orderRepository.findByCustomerIdAndOrderStatusIn(
                    customerId,
                    filter.getStatuses(),
                    getOrdersQuery.getPage(),
                    getOrdersQuery.getSize()
            );
        }

        com.berkay.order.service.domain.ports.output.repository.CustomerOrdersSummaryProjection summary =
                orderRepository.getCustomerOrdersSummary(customerId);
        long allCount = summary != null ? summary.getAllCount() : 0L;
        long activeCount = summary != null ? summary.getActiveCount() : 0L;
        long succeededCount = summary != null ? summary.getSucceededCount() : 0L;
        long abortCount = summary != null ? summary.getAbortCount() : 0L;

        List<Order> orders = pageResult.getOrders();
        List<UUID> restaurantIds = orders.stream()
                .map(order -> order.getRestaurantId().getValue())
                .distinct()
                .collect(Collectors.toList());

        Map<UUID, String> restaurantNames = restaurantRepository.findRestaurantNamesByIds(restaurantIds);

        List<OrderSummary> orderSummaries = orders.stream()
                .map(order -> mapToOrderSummary(order, restaurantNames.get(order.getRestaurantId().getValue())))
                .collect(Collectors.toList());

        return GetOrdersResponse.builder()
                .orders(orderSummaries)
                .page(pageResult.getPage())
                .size(pageResult.getSize())
                .totalElements(pageResult.getTotalElements())
                .totalPages(pageResult.getTotalPages())
                .isLast(pageResult.isLast())
                .activeCount(activeCount)
                .succeededCount(succeededCount)
                .abortCount(abortCount)
                .allCount(allCount)
                .build();
    }

    private OrderSummary mapToOrderSummary(Order order, String restaurantName) {
        String failureMessages = order.getCancelReason() != null
                ? order.getCancelReason().name()
                : (order.getFailureMessages() != null ? String.join(", ", order.getFailureMessages()) : "");

        List<OrderItemSummaryDto> items = order.getItems() != null
                ? order.getItems().stream()
                    .map(item -> OrderItemSummaryDto.builder()
                            .name(item.getProduct() != null && item.getProduct().getName() != null
                                    ? item.getProduct().getName()
                                    : "Product")
                            .quantity(item.getQuantity())
                            .build())
                    .collect(Collectors.toList())
                : List.of();

        int totalItemCount = items.stream().mapToInt(OrderItemSummaryDto::getQuantity).sum();

        return OrderSummary.builder()
                .orderId(order.getId().getValue())
                .orderTrackingId(order.getTrackingId().getValue())
                .restaurantId(order.getRestaurantId().getValue())
                .restaurantName(restaurantName != null && !restaurantName.isBlank() ? restaurantName : "Restoran")
                .orderStatus(order.getOrderStatus())
                .totalAmount(order.getPrice().getAmount())
                .totalItemCount(totalItemCount)
                .items(items)
                .failureMessages(failureMessages)
                .cancelReason(order.getCancelReason())
                .createdAt(order.getCreatedAt())
                .build();
    }
}
