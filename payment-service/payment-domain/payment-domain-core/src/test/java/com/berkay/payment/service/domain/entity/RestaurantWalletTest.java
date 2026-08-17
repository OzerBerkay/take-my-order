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
}
