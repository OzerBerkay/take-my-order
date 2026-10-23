package com.berkay.payment.service.dataaccess.escrow.adapter;

import com.berkay.domain.valueobject.CustomerId;
import com.berkay.domain.valueobject.Money;
import com.berkay.domain.valueobject.OrderId;
import com.berkay.domain.valueobject.RestaurantId;
import com.berkay.payment.service.dataaccess.escrow.entity.OrderEscrowEntity;
import com.berkay.payment.service.dataaccess.escrow.mapper.OrderEscrowDataAccessMapper;
import com.berkay.payment.service.dataaccess.escrow.repository.OrderEscrowJpaRepository;
import com.berkay.payment.service.domain.entity.OrderEscrow;
import com.berkay.payment.service.domain.valueobject.OrderEscrowId;
import com.berkay.payment.service.domain.valueobject.OrderEscrowStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class OrderEscrowRepositoryImplTest {

    @Mock
    private OrderEscrowJpaRepository orderEscrowJpaRepository;

    private OrderEscrowDataAccessMapper orderEscrowDataAccessMapper;
    private OrderEscrowRepositoryImpl orderEscrowRepository;

    private final UUID escrowId = UUID.randomUUID();
    private final UUID orderId = UUID.randomUUID();
    private final UUID restaurantId = UUID.randomUUID();
    private final UUID customerId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        orderEscrowDataAccessMapper = new OrderEscrowDataAccessMapper();
        orderEscrowRepository = new OrderEscrowRepositoryImpl(orderEscrowJpaRepository, orderEscrowDataAccessMapper);
    }

    @Test
    void save_whenNewEntity_persistsEntity() {
        OrderEscrow domain = OrderEscrow.builder()
                .orderEscrowId(new OrderEscrowId(escrowId))
                .orderId(new OrderId(orderId))
                .restaurantId(new RestaurantId(restaurantId))
                .customerId(new CustomerId(customerId))
                .amount(new Money(new BigDecimal("100.00")))
                .status(OrderEscrowStatus.HOLDING)
                .createdAt(ZonedDateTime.now())
                .updatedAt(ZonedDateTime.now())
                .version(0)
                .build();

        when(orderEscrowJpaRepository.findById(escrowId)).thenReturn(Optional.empty());
        when(orderEscrowJpaRepository.save(any(OrderEscrowEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrderEscrow saved = orderEscrowRepository.save(domain);

        verify(orderEscrowJpaRepository).save(any(OrderEscrowEntity.class));
        assertEquals(OrderEscrowStatus.HOLDING, saved.getStatus());
        assertEquals(escrowId, saved.getId().getValue());
    }

    @Test
    void save_whenExistingEntity_updatesInPlace() {
        OrderEscrow domain = OrderEscrow.builder()
                .orderEscrowId(new OrderEscrowId(escrowId))
                .orderId(new OrderId(orderId))
                .restaurantId(new RestaurantId(restaurantId))
                .customerId(new CustomerId(customerId))
                .amount(new Money(new BigDecimal("100.00")))
                .status(OrderEscrowStatus.RELEASED)
                .createdAt(ZonedDateTime.now())
                .updatedAt(ZonedDateTime.now())
                .version(0)
                .build();

        OrderEscrowEntity existingEntity = OrderEscrowEntity.builder()
                .id(escrowId)
                .orderId(orderId)
                .restaurantId(restaurantId)
                .customerId(customerId)
                .amount(new BigDecimal("100.00"))
                .status(OrderEscrowStatus.HOLDING)
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getCreatedAt())
                .version(0)
                .build();

        when(orderEscrowJpaRepository.findById(escrowId)).thenReturn(Optional.of(existingEntity));
        when(orderEscrowJpaRepository.save(any(OrderEscrowEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrderEscrow saved = orderEscrowRepository.save(domain);

        assertEquals(OrderEscrowStatus.RELEASED, saved.getStatus());
        assertEquals(OrderEscrowStatus.RELEASED, existingEntity.getStatus());
    }

    @Test
    void findByOrderId_returnsMappedEntity() {
        OrderEscrowEntity entity = OrderEscrowEntity.builder()
                .id(escrowId)
                .orderId(orderId)
                .restaurantId(restaurantId)
                .customerId(customerId)
                .amount(new BigDecimal("100.00"))
                .status(OrderEscrowStatus.HOLDING)
                .createdAt(ZonedDateTime.now())
                .updatedAt(ZonedDateTime.now())
                .version(0)
                .build();

        when(orderEscrowJpaRepository.findByOrderId(orderId)).thenReturn(Optional.of(entity));

        Optional<OrderEscrow> result = orderEscrowRepository.findByOrderId(new OrderId(orderId));

        assertTrue(result.isPresent());
        assertEquals(OrderEscrowStatus.HOLDING, result.get().getStatus());
        assertEquals(orderId, result.get().getOrderId().getValue());
    }

    @Test
    void getEscrowSummary_delegatesToJpaRepository() {
        java.time.OffsetDateTime startOfDay = java.time.OffsetDateTime.now();
        com.berkay.payment.service.domain.ports.output.repository.OrderEscrowSummaryProjection projection =
                org.mockito.Mockito.mock(com.berkay.payment.service.domain.ports.output.repository.OrderEscrowSummaryProjection.class);

        when(orderEscrowJpaRepository.getEscrowSummary(restaurantId, startOfDay)).thenReturn(projection);

        com.berkay.payment.service.domain.ports.output.repository.OrderEscrowSummaryProjection result =
                orderEscrowRepository.getEscrowSummary(restaurantId, startOfDay);

        assertEquals(projection, result);
        verify(orderEscrowJpaRepository).getEscrowSummary(restaurantId, startOfDay);
    }
}
