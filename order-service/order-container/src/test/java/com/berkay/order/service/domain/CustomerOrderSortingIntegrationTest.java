package com.berkay.order.service.domain;

import com.berkay.domain.valueobject.CustomerId;
import com.berkay.domain.valueobject.OrderStatus;
import com.berkay.order.service.application.OrderServiceApplication;
import com.berkay.order.service.dataaccess.order.entity.OrderAddressEntity;
import com.berkay.order.service.dataaccess.order.entity.OrderEntity;
import com.berkay.order.service.dataaccess.order.repository.OrderJpaRepository;
import com.berkay.order.service.domain.dto.read.OrderPageResult;
import com.berkay.order.service.domain.entity.Order;
import com.berkay.order.service.domain.ports.output.repository.OrderRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@org.springframework.transaction.annotation.Transactional
@SpringBootTest(classes = OrderServiceApplication.class,
        properties = {
                "ORDER_SERVICE_PORT=8182",
                "order-service.restaurant-created-topic-name=restaurant-created",
                "order-service.payment-response-topic-name=payment-response",
                "order-service.payment-request-topic-name=payment-request",
                "order-service.restaurant-approval-request-topic-name=restaurant-approval-request",
                "order-service.restaurant-approval-response-topic-name=restaurant-approval-response",
                "kafka-consumer-config.restaurant-created-consumer-group-id=restaurant-created-topic-consumer-test-sort",
                "kafka-consumer-config.payment-consumer-group-id=payment-topic-consumer-test-sort",
                "kafka-consumer-config.restaurant-approval-consumer-group-id=restaurant-approval-topic-consumer-test-sort"
        }
)
public class CustomerOrderSortingIntegrationTest {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderJpaRepository orderJpaRepository;

    private UUID customerId;
    private UUID restaurantId;
    private OrderEntity orderDelivered;
    private OrderEntity orderCancelled;
    private OrderEntity orderPreparing;
    private OrderEntity orderApproved;

    @BeforeEach
    void setUp() {
        customerId = UUID.randomUUID();
        restaurantId = UUID.randomUUID();
        ZonedDateTime now = ZonedDateTime.now();

        // 1. DELIVERED created 10 minutes ago (past, older)
        orderDelivered = createAndSaveOrder(UUID.randomUUID(), customerId, restaurantId, OrderStatus.DELIVERED, now.minusMinutes(10));

        // 2. CANCELLED created 5 minutes ago (past, newer)
        orderCancelled = createAndSaveOrder(UUID.randomUUID(), customerId, restaurantId, OrderStatus.CANCELLED, now.minusMinutes(5));

        // 3. PREPARING created 30 minutes ago (ACTIVE, older)
        orderPreparing = createAndSaveOrder(UUID.randomUUID(), customerId, restaurantId, OrderStatus.PREPARING, now.minusMinutes(30));

        // 4. APPROVED created 15 minutes ago (ACTIVE, newer)
        orderApproved = createAndSaveOrder(UUID.randomUUID(), customerId, restaurantId, OrderStatus.APPROVED, now.minusMinutes(15));
    }

    @AfterEach
    void tearDown() {
        if (customerId != null) {
            orderJpaRepository.deleteAll(List.of(orderDelivered, orderCancelled, orderPreparing, orderApproved));
        }
    }

    private OrderEntity createAndSaveOrder(UUID id, UUID customerId, UUID restaurantId, OrderStatus status, ZonedDateTime createdAt) {
        OrderAddressEntity address = OrderAddressEntity.builder()
                .id(UUID.randomUUID())
                .city("Istanbul")
                .district("Kadikoy")
                .neighborhood("Caferaga")
                .street("Moda")
                .buildingNumber("1")
                .doorNumber("2")
                .contactFirstName("Ali")
                .contactLastName("Veli")
                .contactPhone("+905551234567")
                .build();

        OrderEntity entity = OrderEntity.builder()
                .id(id)
                .customerId(customerId)
                .restaurantId(restaurantId)
                .trackingId(UUID.randomUUID())
                .price(new BigDecimal("100.00"))
                .deliveryFee(new BigDecimal("15.00"))
                .orderStatus(status)
                .createdAt(createdAt)
                .address(address)
                .items(List.of())
                .build();
        address.setOrder(entity);
        return orderJpaRepository.save(entity);
    }

    @Test
    @DisplayName("findByCustomerId returns active orders first sorted by createdAt DESC, then past orders sorted by createdAt DESC")
    void testActiveOrdersComeFirstInAllFilter() {
        OrderPageResult pageResult = orderRepository.findByCustomerId(new CustomerId(customerId), 0, 10);

        assertNotNull(pageResult);
        assertEquals(4, pageResult.getOrders().size());
        assertEquals(4L, pageResult.getTotalElements());

        List<Order> orders = pageResult.getOrders();

        // 1st: orderApproved (APPROVED, 15m ago) - newer active
        assertEquals(orderApproved.getId(), orders.get(0).getId().getValue());
        assertEquals(OrderStatus.APPROVED, orders.get(0).getOrderStatus());

        // 2nd: orderPreparing (PREPARING, 30m ago) - older active
        assertEquals(orderPreparing.getId(), orders.get(1).getId().getValue());
        assertEquals(OrderStatus.PREPARING, orders.get(1).getOrderStatus());

        // 3rd: orderCancelled (CANCELLED, 5m ago) - newer past
        assertEquals(orderCancelled.getId(), orders.get(2).getId().getValue());
        assertEquals(OrderStatus.CANCELLED, orders.get(2).getOrderStatus());

        // 4th: orderDelivered (DELIVERED, 10m ago) - older past
        assertEquals(orderDelivered.getId(), orders.get(3).getId().getValue());
        assertEquals(OrderStatus.DELIVERED, orders.get(3).getOrderStatus());
    }
}
