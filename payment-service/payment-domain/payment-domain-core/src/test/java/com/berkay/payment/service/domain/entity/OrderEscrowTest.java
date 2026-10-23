package com.berkay.payment.service.domain.entity;

import com.berkay.domain.valueobject.CustomerId;
import com.berkay.domain.valueobject.Money;
import com.berkay.domain.valueobject.OrderId;
import com.berkay.domain.valueobject.RestaurantId;
import com.berkay.payment.service.domain.exception.PaymentDomainException;
import com.berkay.payment.service.domain.valueobject.OrderEscrowStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class OrderEscrowTest {

    private OrderEscrow escrow;
    private final UUID ORDER_ID = UUID.randomUUID();
    private final UUID RESTAURANT_ID = UUID.randomUUID();
    private final UUID CUSTOMER_ID = UUID.randomUUID();
    private final Money AMOUNT = new Money(new BigDecimal("75.00"));

    @BeforeEach
    void setUp() {
        escrow = OrderEscrow.builder()
                .orderId(new OrderId(ORDER_ID))
                .restaurantId(new RestaurantId(RESTAURANT_ID))
                .customerId(new CustomerId(CUSTOMER_ID))
                .amount(AMOUNT)
                .build();
    }

    @Test
    void testInitializeEscrow() {
        escrow.initializeEscrow();

        assertNotNull(escrow.getId());
        assertEquals(OrderEscrowStatus.HOLDING, escrow.getStatus());
        assertEquals(ORDER_ID, escrow.getOrderId().getValue());
        assertEquals(RESTAURANT_ID, escrow.getRestaurantId().getValue());
        assertEquals(CUSTOMER_ID, escrow.getCustomerId().getValue());
        assertEquals(AMOUNT, escrow.getAmount());
        assertNotNull(escrow.getCreatedAt());
        assertNotNull(escrow.getUpdatedAt());
        assertEquals(escrow.getCreatedAt(), escrow.getUpdatedAt());
    }

    @Test
    void testReleaseSuccess() {
        escrow.initializeEscrow();
        escrow.release();

        assertEquals(OrderEscrowStatus.RELEASED, escrow.getStatus());
        assertTrue(escrow.getUpdatedAt().isAfter(escrow.getCreatedAt()) || escrow.getUpdatedAt().isEqual(escrow.getCreatedAt()));
    }

    @Test
    void testRefundSuccess() {
        escrow.initializeEscrow();
        escrow.refund();

        assertEquals(OrderEscrowStatus.REFUNDED, escrow.getStatus());
        assertTrue(escrow.getUpdatedAt().isAfter(escrow.getCreatedAt()) || escrow.getUpdatedAt().isEqual(escrow.getCreatedAt()));
    }

    @Test
    void testReleaseWhenAlreadyReleased_ThrowsException() {
        escrow.initializeEscrow();
        escrow.release();

        assertThrows(PaymentDomainException.class, () -> escrow.release());
    }

    @Test
    void testRefundWhenAlreadyRefunded_ThrowsException() {
        escrow.initializeEscrow();
        escrow.refund();

        assertThrows(PaymentDomainException.class, () -> escrow.refund());
    }

    @Test
    void testReleaseWhenRefunded_ThrowsException() {
        escrow.initializeEscrow();
        escrow.refund();

        assertThrows(PaymentDomainException.class, () -> escrow.release());
    }
}
