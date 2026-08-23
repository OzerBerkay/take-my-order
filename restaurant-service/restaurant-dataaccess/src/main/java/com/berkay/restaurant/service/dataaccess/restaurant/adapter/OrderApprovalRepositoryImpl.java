package com.berkay.restaurant.service.dataaccess.restaurant.adapter;

import com.berkay.restaurant.service.dataaccess.restaurant.mapper.RestaurantDataAccessMapper;
import com.berkay.restaurant.service.dataaccess.restaurant.repository.OrderApprovalJpaRepository;
import com.berkay.restaurant.service.domain.entity.OrderApproval;
import com.berkay.restaurant.service.domain.ports.output.repository.OrderApprovalRepository;
import org.springframework.stereotype.Component;

@Component
public class OrderApprovalRepositoryImpl implements OrderApprovalRepository {

    private final OrderApprovalJpaRepository orderApprovalJpaRepository;
    private final RestaurantDataAccessMapper restaurantDataAccessMapper;

    public OrderApprovalRepositoryImpl(OrderApprovalJpaRepository orderApprovalJpaRepository,
                                       RestaurantDataAccessMapper restaurantDataAccessMapper) {
        this.orderApprovalJpaRepository = orderApprovalJpaRepository;
        this.restaurantDataAccessMapper = restaurantDataAccessMapper;
    }

    @Override
    public OrderApproval save(OrderApproval orderApproval) {
        return restaurantDataAccessMapper
                .orderApprovalEntityToOrderApproval(orderApprovalJpaRepository
                        .save(restaurantDataAccessMapper.orderApprovalToOrderApprovalEntity(orderApproval)));
    }

    @Override
    public java.util.Optional<OrderApproval> findByRestaurantIdAndOrderId(java.util.UUID restaurantId, java.util.UUID orderId) {
        return orderApprovalJpaRepository.findByRestaurantIdAndOrderId(restaurantId, orderId)
                .map(restaurantDataAccessMapper::orderApprovalEntityToOrderApproval);
    }

    @Override
    public java.util.List<OrderApproval> findByRestaurantIdAndStatus(java.util.UUID restaurantId, com.berkay.domain.valueobject.OrderApprovalStatus status) {
        return orderApprovalJpaRepository.findByRestaurantIdAndStatus(restaurantId, status)
                .stream()
                .map(restaurantDataAccessMapper::orderApprovalEntityToOrderApproval)
                .collect(java.util.stream.Collectors.toList());
    }
}