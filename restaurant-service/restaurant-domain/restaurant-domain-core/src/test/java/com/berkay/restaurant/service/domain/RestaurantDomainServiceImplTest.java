package com.berkay.restaurant.service.domain;

import com.berkay.domain.valueobject.Money;
import com.berkay.domain.valueobject.OrderId;
import com.berkay.domain.valueobject.OrderStatus;
import com.berkay.domain.valueobject.ProductId;
import com.berkay.restaurant.service.domain.entity.OrderDetail;
import com.berkay.restaurant.service.domain.entity.Product;
import com.berkay.restaurant.service.domain.entity.Restaurant;
import com.berkay.restaurant.service.domain.event.OrderApprovalEvent;
import com.berkay.restaurant.service.domain.event.OrderRejectedEvent;
import com.berkay.restaurant.service.domain.valueobject.RestaurantName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class RestaurantDomainServiceImplTest {

    private RestaurantDomainService restaurantDomainService;
    private Restaurant restaurant;

    @BeforeEach
    void setUp() {
        restaurantDomainService = new RestaurantDomainServiceImpl();

        Product product = Product.builder()
                .productId(new ProductId(UUID.randomUUID()))
                .name("Test Product")
                .price(new Money(new BigDecimal("10.00")))
                .stock(5)
                .available(true)
                .hidden(false)
                .build();

        restaurant = Restaurant.builder()
                .restaurantName(new RestaurantName("Test Restaurant"))
                .active(true)
                .available(true)
                .minimumOrderAmount(new Money(new BigDecimal("20.00")))
                .deliveryFee(new Money(new BigDecimal("5.00")))
                .menu(new ArrayList<>(List.of(product)))
                .build();
    }

    @Test
    void validateOrder_ShouldReject_WhenProductIsHidden() {
        Product hiddenProduct = restaurant.getMenu().get(0);
        hiddenProduct.updateWith(hiddenProduct.getName(), hiddenProduct.getDescription(), hiddenProduct.getPrice(), hiddenProduct.isAvailable(), hiddenProduct.getStock(), true, hiddenProduct.getImageUrl());

        OrderDetail orderDetail = OrderDetail.builder()
                .orderId(new OrderId(UUID.randomUUID()))
                .orderStatus(OrderStatus.PAID)
                .totalAmount(new Money(new BigDecimal("15.00")))
                .productQuantities(Map.of(hiddenProduct.getId(), 1))
                .build();
        restaurant.initOrderDetail(orderDetail);

        List<String> failureMessages = new ArrayList<>();
        Optional<OrderApprovalEvent> eventOpt = restaurantDomainService.validateOrder(restaurant, failureMessages);

        assertTrue(eventOpt.isPresent());
        assertTrue(eventOpt.get() instanceof OrderRejectedEvent);
        assertTrue(failureMessages.stream().anyMatch(m -> m.contains("is hidden")));
    }

    @Test
    void validateOrder_ShouldReject_WhenStockIsInsufficient() {
        Product product = restaurant.getMenu().get(0);

        OrderDetail orderDetail = OrderDetail.builder()
                .orderId(new OrderId(UUID.randomUUID()))
                .orderStatus(OrderStatus.PAID)
                .totalAmount(new Money(new BigDecimal("65.00"))) // 6 items * 10 + 5 fee
                .productQuantities(Map.of(product.getId(), 6)) // requested 6, stock is 5
                .build();
        restaurant.initOrderDetail(orderDetail);

        List<String> failureMessages = new ArrayList<>();
        Optional<OrderApprovalEvent> eventOpt = restaurantDomainService.validateOrder(restaurant, failureMessages);

        assertTrue(eventOpt.isPresent());
        assertTrue(eventOpt.get() instanceof OrderRejectedEvent);
        assertTrue(failureMessages.stream().anyMatch(m -> m.contains("has insufficient stock")));
        assertEquals(5, product.getStock()); // Stock should not be subtracted
    }


    @Test
    void validateOrder_ShouldReject_WhenBelowMinimumOrderAmount() {
        Product product = restaurant.getMenu().get(0);

        OrderDetail orderDetail = OrderDetail.builder()
                .orderId(new OrderId(UUID.randomUUID()))
                .orderStatus(OrderStatus.PAID)
                .totalAmount(new Money(new BigDecimal("15.00"))) // 1 item * 10 + 5 fee
                .productQuantities(Map.of(product.getId(), 1)) // requested 1, total is 10 < 20
                .build();
        restaurant.initOrderDetail(orderDetail);

        List<String> failureMessages = new ArrayList<>();
        Optional<OrderApprovalEvent> eventOpt = restaurantDomainService.validateOrder(restaurant, failureMessages);

        assertTrue(eventOpt.isPresent());
        assertTrue(eventOpt.get() instanceof OrderRejectedEvent);
        assertTrue(failureMessages.stream().anyMatch(m -> m.contains("Order amount is less than minimum order amount!")));
    }

    @Test
    void validateOrder_ShouldApproveAndSubtractStock_WhenValid() {
        Product product = restaurant.getMenu().get(0);

        OrderDetail orderDetail = OrderDetail.builder()
                .orderId(new OrderId(UUID.randomUUID()))
                .orderStatus(OrderStatus.PAID)
                .totalAmount(new Money(new BigDecimal("35.00"))) // 3 items * 10 + 5 fee
                .productQuantities(Map.of(product.getId(), 3)) // requested 3, stock is 5
                .build();
        restaurant.initOrderDetail(orderDetail);

        List<String> failureMessages = new ArrayList<>();
        Optional<OrderApprovalEvent> eventOpt = restaurantDomainService.validateOrder(restaurant, failureMessages);

        assertFalse(eventOpt.isPresent()); // Expected empty (PENDING state) when valid
        assertTrue(failureMessages.isEmpty());
        assertEquals(2, product.getStock()); // 5 - 3 = 2
    }

    @Test
    void approveOrder_ShouldTransitionToApproved_WhenPending() {
        Product product = restaurant.getMenu().get(0);
        OrderDetail orderDetail = OrderDetail.builder()
                .orderId(new OrderId(UUID.randomUUID()))
                .orderStatus(OrderStatus.PAID)
                .totalAmount(new Money(new BigDecimal("35.00")))
                .productQuantities(Map.of(product.getId(), 3))
                .build();
        restaurant.initOrderDetail(orderDetail);
        
        restaurantDomainService.validateOrder(restaurant, new ArrayList<>());
        
        OrderApprovalEvent event = restaurant.approveOrder();
        
        assertTrue(event instanceof com.berkay.restaurant.service.domain.event.OrderApprovedEvent);
        assertEquals(com.berkay.domain.valueobject.OrderApprovalStatus.APPROVED, restaurant.getOrderApproval().getApprovalStatus());
    }

    @Test
    void rejectOrder_ShouldTransitionToRejected_WhenPending() {
        Product product = restaurant.getMenu().get(0);
        OrderDetail orderDetail = OrderDetail.builder()
                .orderId(new OrderId(UUID.randomUUID()))
                .orderStatus(OrderStatus.PAID)
                .totalAmount(new Money(new BigDecimal("35.00")))
                .productQuantities(Map.of(product.getId(), 3))
                .build();
        restaurant.initOrderDetail(orderDetail);
        
        restaurantDomainService.validateOrder(restaurant, new ArrayList<>());
        
        List<String> failureMessages = List.of("Out of stock suddenly");
        OrderApprovalEvent event = restaurant.rejectOrder(failureMessages);
        
        assertTrue(event instanceof OrderRejectedEvent);
        assertEquals(com.berkay.domain.valueobject.OrderApprovalStatus.REJECTED, restaurant.getOrderApproval().getApprovalStatus());
    }

    @Test
    void approveOrder_ShouldThrowException_WhenNotPending() {
        Product product = restaurant.getMenu().get(0);
        OrderDetail orderDetail = OrderDetail.builder()
                .orderId(new OrderId(UUID.randomUUID()))
                .orderStatus(OrderStatus.PAID)
                .totalAmount(new Money(new BigDecimal("35.00")))
                .productQuantities(Map.of(product.getId(), 3))
                .build();
        restaurant.initOrderDetail(orderDetail);
        
        restaurantDomainService.validateOrder(restaurant, new ArrayList<>());
        restaurant.approveOrder(); // transitions to APPROVED
        
        com.berkay.restaurant.service.domain.exception.RestaurantDomainException exception = assertThrows(com.berkay.restaurant.service.domain.exception.RestaurantDomainException.class, () -> restaurant.approveOrder());
        assertTrue(exception.getMessage().contains("Order is not in PENDING state for approval"));
    }
}
