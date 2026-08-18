package com.berkay.payment.service.domain;

import com.berkay.domain.valueobject.Money;
import com.berkay.payment.service.domain.dto.wallet.WalletBalanceResponse;
import com.berkay.payment.service.domain.dto.wallet.WalletDepositCommand;
import com.berkay.payment.service.domain.dto.wallet.WalletWithdrawCommand;
import com.berkay.payment.service.domain.entity.CustomerWallet;
import com.berkay.payment.service.domain.entity.Wallet;
import com.berkay.payment.service.domain.entity.WalletTransaction;
import com.berkay.payment.service.domain.ports.output.repository.WalletRepository;
import com.berkay.payment.service.domain.ports.output.repository.WalletTransactionRepository;
import com.berkay.payment.service.domain.valueobject.WalletId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import org.springframework.dao.DataIntegrityViolationException;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WalletApplicationServiceImplTest {

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private WalletTransactionRepository walletTransactionRepository;

    @InjectMocks
    private WalletApplicationServiceImpl walletApplicationService;

    private Wallet wallet;
    private final UUID ownerId = UUID.randomUUID();
    private final UUID walletId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        wallet = new CustomerWallet(
                new WalletId(walletId),
                ownerId,
                new Money(new BigDecimal("100.00"))
        );
    }

    @Test
    void testDeposit_ShouldReturnUpdatedBalance() {
        // Given
        WalletDepositCommand command = WalletDepositCommand.builder()
                .ownerId(ownerId)
                .amount(new BigDecimal("50.00"))
                .build();

        when(walletRepository.findByOwnerIdWithLock(ownerId)).thenReturn(Optional.of(wallet));
        when(walletRepository.save(any(Wallet.class))).thenReturn(wallet);

        // When
        WalletBalanceResponse response = walletApplicationService.deposit(command);

        // Then
        assertEquals(ownerId, response.getOwnerId());
        assertEquals(new BigDecimal("150.00"), response.getBalance());
        
        verify(walletRepository, times(1)).findByOwnerIdWithLock(ownerId);
        verify(walletRepository, times(1)).save(wallet);
        verify(walletTransactionRepository, times(1)).save(any(WalletTransaction.class));
    }

    @Test
    void testWithdraw_ShouldReturnUpdatedBalance() {
        // Given
        WalletWithdrawCommand command = WalletWithdrawCommand.builder()
                .ownerId(ownerId)
                .amount(new BigDecimal("20.00"))
                .build();

        when(walletRepository.findByOwnerIdWithLock(ownerId)).thenReturn(Optional.of(wallet));
        when(walletRepository.save(any(Wallet.class))).thenReturn(wallet);

        // When
        WalletBalanceResponse response = walletApplicationService.withdraw(command);

        // Then
        assertEquals(ownerId, response.getOwnerId());
        assertEquals(new BigDecimal("80.00"), response.getBalance());

        verify(walletRepository, times(1)).findByOwnerIdWithLock(ownerId);
        verify(walletRepository, times(1)).save(wallet);
        verify(walletTransactionRepository, times(1)).save(any(WalletTransaction.class));
    }

    @Test
    void testDeposit_WithDuplicateIdempotencyKey_ShouldThrowException() {
        // Given
        WalletDepositCommand command = WalletDepositCommand.builder()
                .ownerId(ownerId)
                .amount(new BigDecimal("50.00"))
                .idempotencyKey("duplicate-key-123")
                .build();

        when(walletRepository.findByOwnerIdWithLock(ownerId)).thenReturn(Optional.of(wallet));
        when(walletTransactionRepository.save(any(WalletTransaction.class)))
                .thenThrow(new DataIntegrityViolationException("Unique index or primary key violation"));

        // When & Then
        assertThrows(DataIntegrityViolationException.class, () -> {
            walletApplicationService.deposit(command);
        });
    }
}
