package com.berkay.payment.service.domain;

import com.berkay.domain.valueobject.Money;
import com.berkay.payment.service.domain.dto.wallet.*;
import com.berkay.payment.service.domain.entity.CustomerWallet;
import com.berkay.payment.service.domain.entity.Wallet;
import com.berkay.payment.service.domain.entity.WalletTransaction;
import com.berkay.payment.service.domain.exception.PaymentApplicationServiceException;
import com.berkay.payment.service.domain.ports.output.repository.OrderEscrowRepository;
import com.berkay.payment.service.domain.ports.output.repository.OrderEscrowSummaryProjection;
import com.berkay.payment.service.domain.ports.output.repository.WalletRepository;
import com.berkay.payment.service.domain.ports.output.repository.WalletTransactionRepository;
import com.berkay.payment.service.domain.valueobject.DomainPagedResult;
import com.berkay.payment.service.domain.valueobject.WalletId;
import com.berkay.payment.service.domain.valueobject.WalletTransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class WalletApplicationServiceImplTest {

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private WalletTransactionRepository walletTransactionRepository;

    @Mock
    private OrderEscrowRepository orderEscrowRepository;

    @InjectMocks
    private WalletApplicationServiceImpl walletApplicationService;

    private final UUID OWNER_ID = UUID.randomUUID();
    private Wallet wallet;

    @BeforeEach
    void setUp() {
        wallet = new CustomerWallet(
                new WalletId(UUID.randomUUID()),
                OWNER_ID,
                new Money(new BigDecimal("100.00"))
        );
    }

    @Test
    void testDepositSuccess() {
        when(walletRepository.findByOwnerIdWithLock(OWNER_ID)).thenReturn(Optional.of(wallet));

        WalletDepositCommand command = WalletDepositCommand.builder()
                .ownerId(OWNER_ID)
                .amount(new BigDecimal("50.00"))
                .idempotencyKey("deposit-1")
                .build();

        WalletBalanceResponse response = walletApplicationService.deposit(command);

        assertEquals(new BigDecimal("150.00"), response.getBalance());
        verify(walletRepository).save(wallet);
        verify(walletTransactionRepository).save(any(WalletTransaction.class));
    }

    @Test
    void testWithdrawSuccess() {
        when(walletRepository.findByOwnerIdWithLock(OWNER_ID)).thenReturn(Optional.of(wallet));

        WalletWithdrawCommand command = WalletWithdrawCommand.builder()
                .ownerId(OWNER_ID)
                .amount(new BigDecimal("40.00"))
                .idempotencyKey("withdraw-1")
                .build();

        WalletBalanceResponse response = walletApplicationService.withdraw(command);

        assertEquals(new BigDecimal("60.00"), response.getBalance());
        verify(walletRepository).save(wallet);
        verify(walletTransactionRepository).save(any(WalletTransaction.class));
    }

    @Test
    void testGetBalanceSuccess() {
        when(walletRepository.findByOwnerId(OWNER_ID)).thenReturn(Optional.of(wallet));

        WalletBalanceResponse response = walletApplicationService.getBalance(OWNER_ID);

        assertEquals(new BigDecimal("100.00"), response.getBalance());
    }

    @Test
    void testGetBalanceNotFound() {
        when(walletRepository.findByOwnerId(OWNER_ID)).thenReturn(Optional.empty());

        assertThrows(PaymentApplicationServiceException.class, () ->
                walletApplicationService.getBalance(OWNER_ID));
    }

    @Test
    void testGetHistoryPagedSuccess() {
        when(walletRepository.findByOwnerId(OWNER_ID)).thenReturn(Optional.of(wallet));

        WalletTransaction tx = WalletTransaction.builder()
                .walletId(wallet.getId())
                .amount(new Money(new BigDecimal("25.00")))
                .transactionType(WalletTransactionType.PAYMENT)
                .referenceId("ref-1")
                .createdAt(ZonedDateTime.now())
                .build();

        DomainPagedResult<WalletTransaction> pagedResult = new DomainPagedResult<>(
                List.of(tx), 0, 10, 1, 1);

        when(walletTransactionRepository.findByWalletIdPaged(wallet.getId().getValue(), WalletTransactionType.PAYMENT, 0, 10))
                .thenReturn(pagedResult);

        WalletHistoryResponse response = walletApplicationService.getHistory(OWNER_ID, WalletTransactionType.PAYMENT, 0, 10);

        assertNotNull(response);
        assertEquals(1, response.getContent().size());
        assertEquals(0, response.getPage());
        assertEquals(1, response.getTotalElements());
        assertTrue(response.isLast());
    }

    @Test
    void testGetFinancialSummarySuccess() {
        when(walletRepository.findByOwnerId(OWNER_ID)).thenReturn(Optional.of(wallet));

        OrderEscrowSummaryProjection projection = mock(OrderEscrowSummaryProjection.class);
        when(projection.getDailySettledRevenue()).thenReturn(new BigDecimal("500.00"));
        when(projection.getPendingPayout()).thenReturn(new BigDecimal("250.00"));
        when(projection.getDailyRefundedAmount()).thenReturn(new BigDecimal("50.00"));

        when(orderEscrowRepository.getEscrowSummary(eq(OWNER_ID), any())).thenReturn(projection);

        RestaurantFinancialSummaryResponse response = walletApplicationService.getFinancialSummary(OWNER_ID);

        assertEquals(new BigDecimal("100.00"), response.getWithdrawableBalance());
        assertEquals(new BigDecimal("500.00"), response.getDailySettledRevenue());
        assertEquals(new BigDecimal("250.00"), response.getPendingPayout());
        assertEquals(new BigDecimal("50.00"), response.getDailyRefundedAmount());
    }
}
