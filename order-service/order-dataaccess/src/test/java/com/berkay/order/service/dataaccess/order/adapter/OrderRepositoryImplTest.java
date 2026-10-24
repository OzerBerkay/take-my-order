package com.berkay.order.service.dataaccess.order.adapter;

import com.berkay.domain.valueobject.CustomerId;
import com.berkay.domain.valueobject.OrderStatus;
import com.berkay.order.service.dataaccess.order.entity.OrderEntity;
import com.berkay.order.service.dataaccess.order.mapper.OrderDataAccessMapper;
import com.berkay.order.service.dataaccess.order.repository.OrderJpaRepository;
import com.berkay.order.service.domain.dto.read.OrderFilterGroup;
import com.berkay.order.service.domain.dto.read.OrderPageResult;
import com.berkay.order.service.domain.entity.Order;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderRepositoryImplTest {

    @Mock
    private OrderJpaRepository orderJpaRepository;

    @Mock
    private OrderDataAccessMapper orderDataAccessMapper;

    private OrderRepositoryImpl orderRepository;

    @BeforeEach
    void setUp() {
        orderRepository = new OrderRepositoryImpl(orderJpaRepository, orderDataAccessMapper);
    }

    @Test
    @DisplayName("findByCustomerId delegates to findByCustomerIdWithActivePriority with ACTIVE statuses")
    void testFindByCustomerIdWithActivePriority() {
        UUID customerUuid = UUID.randomUUID();
        CustomerId customerId = new CustomerId(customerUuid);

        OrderEntity activeOrderEntity = OrderEntity.builder().id(UUID.randomUUID()).orderStatus(OrderStatus.PREPARING).build();
        OrderEntity pastOrderEntity = OrderEntity.builder().id(UUID.randomUUID()).orderStatus(OrderStatus.DELIVERED).build();

        Page<OrderEntity> pagedEntities = new PageImpl<>(List.of(activeOrderEntity, pastOrderEntity), Pageable.ofSize(10), 2);

        Order activeOrderDomain = mock(Order.class);
        Order pastOrderDomain = mock(Order.class);

        when(orderDataAccessMapper.orderEntityToOrder(activeOrderEntity)).thenReturn(activeOrderDomain);
        when(orderDataAccessMapper.orderEntityToOrder(pastOrderEntity)).thenReturn(pastOrderDomain);

        when(orderJpaRepository.findByCustomerIdWithActivePriority(eq(customerUuid), eq(OrderFilterGroup.ACTIVE.getStatuses()), any(Pageable.class)))
                .thenReturn(pagedEntities);

        OrderPageResult result = orderRepository.findByCustomerId(customerId, 0, 10);

        assertNotNull(result);
        assertEquals(2, result.getOrders().size());
        assertEquals(0, result.getPage());
        assertEquals(10, result.getSize());
        assertEquals(2L, result.getTotalElements());

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Collection<OrderStatus>> statusesCaptor = ArgumentCaptor.forClass(Collection.class);
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);

        verify(orderJpaRepository).findByCustomerIdWithActivePriority(
                eq(customerUuid),
                statusesCaptor.capture(),
                pageableCaptor.capture()
        );

        assertTrue(statusesCaptor.getValue().containsAll(OrderFilterGroup.ACTIVE.getStatuses()));
        assertEquals(OrderFilterGroup.ACTIVE.getStatuses().size(), statusesCaptor.getValue().size());
        assertEquals(0, pageableCaptor.getValue().getPageNumber());
        assertEquals(10, pageableCaptor.getValue().getPageSize());
    }

    @Test
    @DisplayName("findByCustomerIdAndOrderStatusIn uses findByCustomerIdAndOrderStatusIn with sorted pageable")
    void testFindByCustomerIdAndOrderStatusIn() {
        UUID customerUuid = UUID.randomUUID();
        CustomerId customerId = new CustomerId(customerUuid);
        List<OrderStatus> statuses = List.of(OrderStatus.DELIVERED);

        OrderEntity deliveredEntity = OrderEntity.builder().id(UUID.randomUUID()).orderStatus(OrderStatus.DELIVERED).build();
        Page<OrderEntity> pagedEntities = new PageImpl<>(List.of(deliveredEntity), Pageable.ofSize(10), 1);
        Order deliveredOrder = mock(Order.class);

        when(orderDataAccessMapper.orderEntityToOrder(deliveredEntity)).thenReturn(deliveredOrder);
        when(orderJpaRepository.findByCustomerIdAndOrderStatusIn(eq(customerUuid), eq(statuses), any(Pageable.class)))
                .thenReturn(pagedEntities);

        OrderPageResult result = orderRepository.findByCustomerIdAndOrderStatusIn(customerId, statuses, 0, 10);

        assertNotNull(result);
        assertEquals(1, result.getOrders().size());

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(orderJpaRepository).findByCustomerIdAndOrderStatusIn(eq(customerUuid), eq(statuses), pageableCaptor.capture());
        assertNotNull(pageableCaptor.getValue().getSort().getOrderFor("createdAt"));
        assertTrue(pageableCaptor.getValue().getSort().getOrderFor("createdAt").isDescending());
    }
}
