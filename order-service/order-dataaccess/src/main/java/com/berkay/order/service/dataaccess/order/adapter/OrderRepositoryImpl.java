package com.berkay.order.service.dataaccess.order.adapter;

import com.berkay.domain.valueobject.OrderId;
import com.berkay.order.service.dataaccess.order.mapper.OrderDataAccessMapper;
import com.berkay.order.service.dataaccess.order.repository.OrderJpaRepository;
import com.berkay.order.service.domain.entity.Order;
import com.berkay.order.service.domain.ports.output.repository.OrderRepository;
import com.berkay.order.service.domain.valueobject.TrackingId;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class OrderRepositoryImpl implements OrderRepository {

    private final OrderJpaRepository orderJpaRepository;
    private final OrderDataAccessMapper orderDataAccessMapper;

    public OrderRepositoryImpl(OrderJpaRepository orderJpaRepository,
                               OrderDataAccessMapper orderDataAccessMapper) {
        this.orderJpaRepository = orderJpaRepository;
        this.orderDataAccessMapper = orderDataAccessMapper;
    }

    @Override
    public Order save(Order order) {
        return orderDataAccessMapper.orderEntityToOrder(orderJpaRepository
                .save(orderDataAccessMapper.orderToOrderEntity(order)));
    }

    @Override
    public Optional<Order> findById(OrderId orderId) {
        return orderJpaRepository.findById(orderId.getValue()).map(orderDataAccessMapper::orderEntityToOrder);
    }

    @Override
    public Optional<Order> findByTrackingId(TrackingId trackingId) {
        return orderJpaRepository.findByTrackingId(trackingId.getValue())
                .map(orderDataAccessMapper::orderEntityToOrder);
    }

    @Override
    public com.berkay.order.service.domain.dto.read.OrderPageResult findByCustomerId(com.berkay.domain.valueobject.CustomerId customerId, int page, int size) {
        org.springframework.data.domain.PageRequest pageRequest = org.springframework.data.domain.PageRequest.of(page, size);
        org.springframework.data.domain.Page<com.berkay.order.service.dataaccess.order.entity.OrderEntity> pagedResult =
                orderJpaRepository.findByCustomerId(customerId.getValue(), pageRequest);

        java.util.List<Order> orders = pagedResult.getContent().stream()
                .map(orderDataAccessMapper::orderEntityToOrder)
                .collect(java.util.stream.Collectors.toList());

        return new com.berkay.order.service.domain.dto.read.OrderPageResult(
                orders,
                pagedResult.getNumber(),
                pagedResult.getSize(),
                pagedResult.getTotalElements(),
                pagedResult.getTotalPages(),
                pagedResult.isLast()
        );
    }
    
    @Override
    public Optional<java.util.List<Order>> findByRestaurantIdAndOrderStatus(com.berkay.domain.valueobject.RestaurantId restaurantId, com.berkay.domain.valueobject.OrderStatus orderStatus) {
        return orderJpaRepository.findByRestaurantIdAndOrderStatus(restaurantId.getValue(), orderStatus)
                .map(orderEntities -> orderEntities.stream()
                        .map(orderDataAccessMapper::orderEntityToOrder)
                        .collect(java.util.stream.Collectors.toList()));
    }
}