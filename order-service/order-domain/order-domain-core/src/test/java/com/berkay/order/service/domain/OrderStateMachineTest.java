package com.berkay.order.service.domain;

import com.berkay.domain.valueobject.*;
import com.berkay.order.service.domain.entity.Order;
import com.berkay.order.service.domain.entity.OrderItem;
import com.berkay.order.service.domain.entity.Product;
import com.berkay.order.service.domain.exception.OrderDomainException;
import com.berkay.order.service.domain.valueobject.OrderItemId;
import com.berkay.order.service.domain.valueobject.StreetAddress;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class OrderStateMachineTest {

    private Order order;

    @BeforeEach
    void setUp() {
        UUID productId = UUID.randomUUID();
        Product product = new Product(new ProductId(productId), "Burger", new Money(new BigDecimal("100.00")), true, false);
        OrderItem orderItem = OrderItem.builder()
                .orderItemId(new OrderItemId(1L))
                .product(product)
                .price(new Money(new BigDecimal("100.00")))
                .quantity(1)
                .subTotal(new Money(new BigDecimal("100.00")))
                .build();

        StreetAddress address = new StreetAddress(UUID.randomUUID(), "Istanbul", "Kadikoy", "Moda", "Moda Cad", "1", "2", 3, "Zil calmayin", "Ahmet", "Yilmaz", "5551234567");

        order = Order.builder()
                .customerId(new CustomerId(UUID.randomUUID()))
                .restaurantId(new RestaurantId(UUID.randomUUID()))
                .deliveryAddress(address)
                .price(new Money(new BigDecimal("100.00")))
                .deliveryFee(new Money(BigDecimal.ZERO))
                .items(List.of(orderItem))
                .build();

        order.initializeOrder();
    }

    @Test
    @DisplayName("Happy path: Full lifecycle from PENDING to DELIVERED")
    void testHappyPathLifecycle() {
        assertEquals(OrderStatus.PENDING, order.getOrderStatus());

        order.pay();
        assertEquals(OrderStatus.PAID, order.getOrderStatus());

        order.waitForApproval();
        assertEquals(OrderStatus.WAITING_FOR_APPROVAL, order.getOrderStatus());

        order.approve();
        assertEquals(OrderStatus.APPROVED, order.getOrderStatus());

        order.startPreparing();
        assertEquals(OrderStatus.PREPARING, order.getOrderStatus());

        order.readyForPickup();
        assertEquals(OrderStatus.READY_FOR_PICKUP, order.getOrderStatus());

        order.startDelivery();
        assertEquals(OrderStatus.IN_DELIVERY, order.getOrderStatus());

        order.deliver();
        assertEquals(OrderStatus.DELIVERED, order.getOrderStatus());
    }

    @Test
    @DisplayName("Invalid transition: Cannot approve before waiting for approval")
    void testCannotApproveDirectlyFromPaid() {
        order.pay();
        assertThrows(OrderDomainException.class, () -> order.approve());
    }

    @Test
    @DisplayName("Invalid transition: Cannot start preparing before approval")
    void testCannotStartPreparingBeforeApproval() {
        order.pay();
        order.waitForApproval();
        assertThrows(OrderDomainException.class, () -> order.startPreparing());
    }

    @Test
    @DisplayName("Cancellation with INSUFFICIENT_STOCK must not allow RESTORE_STOCK (Phantom stock protection)")
    void testPhantomStockProtection() {
        order.pay();
        order.waitForApproval();

        assertThrows(OrderDomainException.class, () ->
                order.initCancel(CancelReason.INSUFFICIENT_STOCK, MaterialAction.RESTORE_STOCK, "Stok bitti"));
    }

    @Test
    @DisplayName("Cancellation with INSUFFICIENT_STOCK and RECORD_WASTE succeeds")
    void testValidStockCancellation() {
        order.pay();
        order.waitForApproval();

        order.initCancel(CancelReason.INSUFFICIENT_STOCK, MaterialAction.RECORD_WASTE, "Taze kofte kalmadi");
        assertEquals(OrderStatus.CANCELLING, order.getOrderStatus());
        assertEquals(CancelReason.INSUFFICIENT_STOCK, order.getCancelReason());
        assertEquals(MaterialAction.RECORD_WASTE, order.getMaterialAction());

        order.cancel(CancelReason.INSUFFICIENT_STOCK);
        assertEquals(OrderStatus.CANCELLED, order.getOrderStatus());
    }

    @Test
    @DisplayName("Direct cancellation from PENDING on payment failure (0 TL refund)")
    void testPaymentFailureDirectCancellation() {
        assertEquals(OrderStatus.PENDING, order.getOrderStatus());

        order.cancel(CancelReason.PAYMENT_FAILED);
        assertEquals(OrderStatus.CANCELLED, order.getOrderStatus());
        assertEquals(CancelReason.PAYMENT_FAILED, order.getCancelReason());
    }

    @Test
    @DisplayName("No-refund cancellation in IN_DELIVERY for CUSTOMER_UNREACHABLE")
    void testNoRefundCustomerUnreachable() {
        order.pay();
        order.waitForApproval();
        order.approve();
        order.startPreparing();
        order.readyForPickup();
        order.startDelivery();

        assertEquals(OrderStatus.IN_DELIVERY, order.getOrderStatus());

        order.cancelWithoutRefund(CancelReason.CUSTOMER_UNREACHABLE, MaterialAction.RECORD_WASTE, "Musteri kapida yok");
        assertEquals(OrderStatus.CANCELLED, order.getOrderStatus());
        assertEquals(CancelReason.CUSTOMER_UNREACHABLE, order.getCancelReason());
        assertEquals(MaterialAction.RECORD_WASTE, order.getMaterialAction());
    }

    @Test
    @DisplayName("No-refund cancellation fails if reason is not eligible")
    void testNoRefundIneligibleReasonFails() {
        order.pay();
        order.waitForApproval();
        order.approve();
        order.startPreparing();
        order.readyForPickup();
        order.startDelivery();

        assertThrows(OrderDomainException.class, () ->
                order.cancelWithoutRefund(CancelReason.OPERATIONAL_DISRUPTIONS, MaterialAction.RECORD_WASTE, "Firin bozuldu"));
    }
}
