package com.berkay.order.service.domain;

import com.berkay.domain.valueobject.RestaurantId;
import com.berkay.order.service.domain.dto.read.OrderPageResult;
import com.berkay.order.service.domain.dto.restaurant.RestaurantOrderResponse;
import com.berkay.order.service.domain.dto.restaurant.RestaurantOrdersPageResponse;
import com.berkay.order.service.domain.dto.restaurant.RestaurantOrdersQuery;
import com.berkay.order.service.domain.exception.OrderDomainException;
import com.berkay.order.service.domain.mapper.OrderDataMapper;
import com.berkay.order.service.domain.ports.output.repository.OrderRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Component
public class RestaurantOrdersQueryHandler {

    private final OrderRepository orderRepository;
    private final OrderDataMapper orderDataMapper;

    public RestaurantOrdersQueryHandler(OrderRepository orderRepository,
                                       OrderDataMapper orderDataMapper) {
        this.orderRepository = orderRepository;
        this.orderDataMapper = orderDataMapper;
    }

    @Transactional(readOnly = true)
    public RestaurantOrdersPageResponse getRestaurantOrders(RestaurantOrdersQuery query) {
        if (query.getSearch() != null && !query.getSearch().isBlank() && query.getSearch().trim().length() < 3) {
            log.warn("Search query parameter is too short: '{}'", query.getSearch());
            throw new OrderDomainException("Arama ifadesi en az 3 karakter olmalıdır!");
        }

        OrderPageResult pageResult = orderRepository.findRestaurantOrders(
                new RestaurantId(query.getRestaurantId()),
                query.getStatus(),
                query.getSearch(),
                query.getPage(),
                query.getSize(),
                query.getSort());

        List<RestaurantOrderResponse> responses = pageResult.getOrders().stream()
                .map(orderDataMapper::orderToRestaurantOrderResponse)
                .toList();

        return RestaurantOrdersPageResponse.builder()
                .content(responses)
                .page(pageResult.getPage())
                .size(pageResult.getSize())
                .totalElements(pageResult.getTotalElements())
                .totalPages(pageResult.getTotalPages())
                .last(pageResult.isLast())
                .build();
    }
}
