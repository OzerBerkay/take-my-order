package com.berkay.payment.service.dataaccess.escrow.mapper;

import com.berkay.domain.valueobject.CustomerId;
import com.berkay.domain.valueobject.Money;
import com.berkay.domain.valueobject.OrderId;
import com.berkay.domain.valueobject.RestaurantId;
import com.berkay.payment.service.dataaccess.escrow.entity.OrderEscrowEntity;
import com.berkay.payment.service.domain.entity.OrderEscrow;
import com.berkay.payment.service.domain.valueobject.OrderEscrowId;
import org.springframework.stereotype.Component;

@Component
public class OrderEscrowDataAccessMapper {

    public OrderEscrowEntity orderEscrowToOrderEscrowEntity(OrderEscrow orderEscrow) {
        return OrderEscrowEntity.builder()
                .id(orderEscrow.getId().getValue())
                .orderId(orderEscrow.getOrderId().getValue())
                .restaurantId(orderEscrow.getRestaurantId().getValue())
                .customerId(orderEscrow.getCustomerId().getValue())
                .amount(orderEscrow.getAmount().getAmount())
                .status(orderEscrow.getStatus())
                .createdAt(orderEscrow.getCreatedAt())
                .updatedAt(orderEscrow.getUpdatedAt())
                .version(orderEscrow.getVersion())
                .build();
    }

    public OrderEscrow orderEscrowEntityToOrderEscrow(OrderEscrowEntity orderEscrowEntity) {
        return OrderEscrow.builder()
                .orderEscrowId(new OrderEscrowId(orderEscrowEntity.getId()))
                .orderId(new OrderId(orderEscrowEntity.getOrderId()))
                .restaurantId(new RestaurantId(orderEscrowEntity.getRestaurantId()))
                .customerId(new CustomerId(orderEscrowEntity.getCustomerId()))
                .amount(new Money(orderEscrowEntity.getAmount()))
                .status(orderEscrowEntity.getStatus())
                .createdAt(orderEscrowEntity.getCreatedAt())
                .updatedAt(orderEscrowEntity.getUpdatedAt())
                .version(orderEscrowEntity.getVersion())
                .build();
    }
}
