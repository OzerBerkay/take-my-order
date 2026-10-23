package com.berkay.payment.service.dataaccess.escrow.adapter;

import com.berkay.domain.valueobject.OrderId;
import com.berkay.payment.service.dataaccess.escrow.entity.OrderEscrowEntity;
import com.berkay.payment.service.dataaccess.escrow.mapper.OrderEscrowDataAccessMapper;
import com.berkay.payment.service.dataaccess.escrow.repository.OrderEscrowJpaRepository;
import com.berkay.payment.service.domain.entity.OrderEscrow;
import com.berkay.payment.service.domain.ports.output.repository.OrderEscrowRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class OrderEscrowRepositoryImpl implements OrderEscrowRepository {

    private final OrderEscrowJpaRepository orderEscrowJpaRepository;
    private final OrderEscrowDataAccessMapper orderEscrowDataAccessMapper;

    public OrderEscrowRepositoryImpl(OrderEscrowJpaRepository orderEscrowJpaRepository,
                                     OrderEscrowDataAccessMapper orderEscrowDataAccessMapper) {
        this.orderEscrowJpaRepository = orderEscrowJpaRepository;
        this.orderEscrowDataAccessMapper = orderEscrowDataAccessMapper;
    }

    @Override
    public OrderEscrow save(OrderEscrow orderEscrow) {
        Optional<OrderEscrowEntity> existingEntity = orderEscrowJpaRepository.findById(orderEscrow.getId().getValue());
        OrderEscrowEntity entity;
        if (existingEntity.isPresent()) {
            entity = existingEntity.get();
            entity.setStatus(orderEscrow.getStatus());
            entity.setUpdatedAt(orderEscrow.getUpdatedAt());
        } else {
            entity = orderEscrowDataAccessMapper.orderEscrowToOrderEscrowEntity(orderEscrow);
        }
        return orderEscrowDataAccessMapper.orderEscrowEntityToOrderEscrow(orderEscrowJpaRepository.save(entity));
    }

    @Override
    public Optional<OrderEscrow> findByOrderId(OrderId orderId) {
        return orderEscrowJpaRepository.findByOrderId(orderId.getValue())
                .map(orderEscrowDataAccessMapper::orderEscrowEntityToOrderEscrow);
    }

    @Override
    public Optional<OrderEscrow> findByOrderIdWithLock(OrderId orderId) {
        return orderEscrowJpaRepository.findByOrderIdWithLock(orderId.getValue())
                .map(orderEscrowDataAccessMapper::orderEscrowEntityToOrderEscrow);
    }

    @Override
    public com.berkay.payment.service.domain.ports.output.repository.OrderEscrowSummaryProjection getEscrowSummary(
            java.util.UUID restaurantId, java.time.OffsetDateTime startOfDay) {
        return orderEscrowJpaRepository.getEscrowSummary(restaurantId, startOfDay);
    }
}
