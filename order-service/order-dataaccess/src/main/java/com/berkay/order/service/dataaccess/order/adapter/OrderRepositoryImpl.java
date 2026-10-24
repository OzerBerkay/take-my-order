package com.berkay.order.service.dataaccess.order.adapter;

import com.berkay.domain.valueobject.OrderId;
import com.berkay.order.service.dataaccess.order.mapper.OrderDataAccessMapper;
import com.berkay.order.service.dataaccess.order.repository.OrderJpaRepository;
import com.berkay.order.service.domain.entity.Order;
import com.berkay.order.service.domain.ports.output.repository.OrderRepository;
import com.berkay.order.service.domain.valueobject.TrackingId;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

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
    public Optional<Order> findByIdWithLock(OrderId orderId) {
        return orderJpaRepository.findByIdWithLock(orderId.getValue()).map(orderDataAccessMapper::orderEntityToOrder);
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
                orderJpaRepository.findByCustomerIdWithActivePriority(
                        customerId.getValue(),
                        com.berkay.order.service.domain.dto.read.OrderFilterGroup.ACTIVE.getStatuses(),
                        pageRequest);

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
    public com.berkay.order.service.domain.dto.read.OrderPageResult findByCustomerIdAndOrderStatusIn(
            com.berkay.domain.valueobject.CustomerId customerId,
            java.util.List<com.berkay.domain.valueobject.OrderStatus> statuses,
            int page,
            int size) {
        org.springframework.data.domain.PageRequest pageRequest = org.springframework.data.domain.PageRequest.of(
                page, size, org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "createdAt"));
        org.springframework.data.domain.Page<com.berkay.order.service.dataaccess.order.entity.OrderEntity> pagedResult =
                orderJpaRepository.findByCustomerIdAndOrderStatusIn(customerId.getValue(), statuses, pageRequest);

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
    public com.berkay.order.service.domain.ports.output.repository.CustomerOrdersSummaryProjection getCustomerOrdersSummary(
            com.berkay.domain.valueobject.CustomerId customerId) {
        return orderJpaRepository.getCustomerOrdersSummary(customerId.getValue());
    }
    
    @Override
    public Optional<java.util.List<Order>> findByRestaurantIdAndOrderStatus(com.berkay.domain.valueobject.RestaurantId restaurantId, com.berkay.domain.valueobject.OrderStatus orderStatus) {
        return orderJpaRepository.findByRestaurantIdAndOrderStatus(restaurantId.getValue(), orderStatus)
                .map(orderEntities -> orderEntities.stream()
                        .map(orderDataAccessMapper::orderEntityToOrder)
                        .collect(java.util.stream.Collectors.toList()));
    }

    @Override
    public com.berkay.order.service.domain.dto.read.OrderPageResult findRestaurantOrders(com.berkay.domain.valueobject.RestaurantId restaurantId,
                                                                                  com.berkay.domain.valueobject.OrderStatus status,
                                                                                  String search,
                                                                                  int page,
                                                                                  int size,
                                                                                  String sort) {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size);
        java.util.List<com.berkay.domain.valueobject.OrderStatus> darkAreaStatuses = java.util.List.of(
                com.berkay.domain.valueobject.OrderStatus.PENDING,
                com.berkay.domain.valueobject.OrderStatus.PAID
        );
        org.springframework.data.jpa.domain.Specification<com.berkay.order.service.dataaccess.order.entity.OrderEntity> spec =
                com.berkay.order.service.dataaccess.order.specification.OrderSpecifications.createRestaurantFilter(
                        restaurantId.getValue(), status, search, darkAreaStatuses, sort);

        org.springframework.data.domain.Page<com.berkay.order.service.dataaccess.order.entity.OrderEntity> pagedOrders =
                orderJpaRepository.findAll(spec, pageable);

        java.util.List<Order> orders = pagedOrders.getContent().stream()
                .map(orderDataAccessMapper::orderEntityToOrder)
                .collect(java.util.stream.Collectors.toList());

        return new com.berkay.order.service.domain.dto.read.OrderPageResult(
                orders,
                pagedOrders.getNumber(),
                pagedOrders.getSize(),
                pagedOrders.getTotalElements(),
                pagedOrders.getTotalPages(),
                pagedOrders.isLast()
        );
    }

    @Override
    public Optional<com.berkay.order.service.domain.ports.output.repository.OrderAuthProjection> findAuthProjectionById(UUID orderId) {
        return orderJpaRepository.findAuthProjectionById(orderId);
    }

    @Override
    public com.berkay.order.service.domain.ports.output.repository.RestaurantOrdersSummaryProjection getRestaurantOrdersSummary(
            com.berkay.domain.valueobject.RestaurantId restaurantId) {
        return orderJpaRepository.getRestaurantOrdersSummary(restaurantId.getValue());
    }
}