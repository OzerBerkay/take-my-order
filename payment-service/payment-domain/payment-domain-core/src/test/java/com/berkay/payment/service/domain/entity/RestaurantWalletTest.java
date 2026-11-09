package com.berkay.payment.service.domain.entity;

import com.berkay.domain.valueobject.Money;
import com.berkay.payment.service.domain.exception.PaymentDomainException;
import com.berkay.payment.service.domain.valueobject.WalletId;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class RestaurantWalletTest {

    @Test
    void testSubtractBalance_WithSufficientBalance_ShouldSubtractSuccessfully() {
        // Given
        RestaurantWallet wallet = new RestaurantWallet(
                new WalletId(UUID.randomUUID()),
                UUID.randomUUID(),
                new Money(new BigDecimal("100.00"))
        );

        // When
        wallet.subtractBalance(new Money(new BigDecimal("40.00")));

        // Then
        assertEquals(new BigDecimal("60.00"), wallet.getBalance().getAmount());
    }

    @Test
    void testSubtractBalance_WithInsufficientBalance_ShouldThrowException() {
        // Given
        RestaurantWallet wallet = new RestaurantWallet(
                new WalletId(UUID.randomUUID()),
                UUID.randomUUID(),
                new Money(new BigDecimal("100.00"))
        );

        // When / Then
        PaymentDomainException exception = assertThrows(PaymentDomainException.class, () -> {
            wallet.subtractBalance(new Money(new BigDecimal("150.00")));
        });
        
        assertEquals("Restaurant does not have enough balance for this transaction!", exception.getMessage());
    }

    @Test
    void testAddBalance_WithMaxAllowedAmount_ShouldAddSuccessfully() {
        // Given
        RestaurantWallet wallet = new RestaurantWallet(
                new WalletId(UUID.randomUUID()),
                UUID.randomUUID(),
                new Money(new BigDecimal("100.00"))
        );

        // When
        wallet.addBalance(new Money(new BigDecimal("99999.99")));

        // Then
        assertEquals(new BigDecimal("100099.99"), wallet.getBalance().getAmount());
    }

    @Test
    void testAddBalance_ExceedingMaxAllowedAmount_ShouldThrowException() {
        // Given
        RestaurantWallet wallet = new RestaurantWallet(
                new WalletId(UUID.randomUUID()),
                UUID.randomUUID(),
                new Money(new BigDecimal("100.00"))
        );

        // When / Then
        PaymentDomainException exception = assertThrows(PaymentDomainException.class, () -> {
            wallet.addBalance(new Money(new BigDecimal("100000.00")));
        });

        assertEquals("Deposit amount cannot exceed 99999.99!", exception.getMessage());
    }

    @Test
    void testAddBalance_WithZeroOrNegativeAmount_ShouldThrowException() {
        // Given
        RestaurantWallet wallet = new RestaurantWallet(
                new WalletId(UUID.randomUUID()),
                UUID.randomUUID(),
                new Money(new BigDecimal("100.00"))
        );

        // When / Then
        assertThrows(PaymentDomainException.class, () -> wallet.addBalance(new Money(BigDecimal.ZERO)));
        assertThrows(PaymentDomainException.class, () -> wallet.addBalance(new Money(new BigDecimal("-10.00"))));
        assertThrows(PaymentDomainException.class, () -> wallet.addBalance(null));
    }
}
