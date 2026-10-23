package com.berkay.payment.service.domain;

import com.berkay.domain.valueobject.Money;
import com.berkay.payment.service.domain.dto.wallet.*;
import com.berkay.payment.service.domain.entity.Wallet;
import com.berkay.payment.service.domain.entity.WalletTransaction;
import com.berkay.payment.service.domain.exception.PaymentApplicationServiceException;
import com.berkay.payment.service.domain.ports.output.repository.OrderEscrowRepository;
import com.berkay.payment.service.domain.ports.output.repository.OrderEscrowSummaryProjection;
import com.berkay.payment.service.domain.ports.output.repository.WalletRepository;
import com.berkay.payment.service.domain.ports.output.repository.WalletTransactionRepository;
import com.berkay.payment.service.domain.valueobject.DomainPagedResult;
import com.berkay.payment.service.domain.valueobject.WalletTransactionType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.berkay.domain.DomainConstants.UTC;

@Slf4j
@Validated
@Service
public class WalletApplicationServiceImpl implements WalletApplicationService {

    private final WalletRepository walletRepository;
    private final WalletTransactionRepository walletTransactionRepository;
    private final OrderEscrowRepository orderEscrowRepository;

    public WalletApplicationServiceImpl(WalletRepository walletRepository,
                                        WalletTransactionRepository walletTransactionRepository,
                                        OrderEscrowRepository orderEscrowRepository) {
        this.walletRepository = walletRepository;
        this.walletTransactionRepository = walletTransactionRepository;
        this.orderEscrowRepository = orderEscrowRepository;
    }

    @Override
    @Transactional
    public WalletBalanceResponse deposit(WalletDepositCommand command) {
        Wallet wallet = getWalletWithLock(command.getOwnerId());
        wallet.addBalance(new Money(command.getAmount()));
        walletRepository.save(wallet);

        WalletTransaction transaction = WalletTransaction.builder()
                .walletId(wallet.getId())
                .amount(new Money(command.getAmount()))
                .transactionType(WalletTransactionType.DEPOSIT)
                .idempotencyKey(command.getIdempotencyKey())
                .createdAt(ZonedDateTime.now(ZoneId.of(UTC)))
                .build();
        walletTransactionRepository.save(transaction);
        log.info("Deposited {} to wallet owner: {}", command.getAmount(), command.getOwnerId());
        return WalletBalanceResponse.builder()
                .ownerId(command.getOwnerId())
                .balance(wallet.getBalance().getAmount())
                .build();
    }

    @Override
    @Transactional
    public WalletBalanceResponse withdraw(WalletWithdrawCommand command) {
        Wallet wallet = getWalletWithLock(command.getOwnerId());
        wallet.subtractBalance(new Money(command.getAmount()));
        walletRepository.save(wallet);

        WalletTransaction transaction = WalletTransaction.builder()
                .walletId(wallet.getId())
                .amount(new Money(command.getAmount()))
                .transactionType(WalletTransactionType.WITHDRAWAL)
                .idempotencyKey(command.getIdempotencyKey())
                .createdAt(ZonedDateTime.now(ZoneId.of(UTC)))
                .build();
        walletTransactionRepository.save(transaction);
        log.info("Withdrew {} from wallet owner: {}", command.getAmount(), command.getOwnerId());
        return WalletBalanceResponse.builder()
                .ownerId(command.getOwnerId())
                .balance(wallet.getBalance().getAmount())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public WalletBalanceResponse getBalance(UUID ownerId) {
        Wallet wallet = getWallet(ownerId);
        return WalletBalanceResponse.builder()
                .ownerId(ownerId)
                .balance(wallet.getBalance().getAmount())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public WalletHistoryResponse getHistory(UUID ownerId) {
        return getHistory(ownerId, null, 0, 10);
    }

    @Override
    @Transactional(readOnly = true)
    public WalletHistoryResponse getHistory(UUID ownerId, WalletTransactionType type, int page, int size) {
        Wallet wallet = getWallet(ownerId);
        DomainPagedResult<WalletTransaction> pagedResult = walletTransactionRepository
                .findByWalletIdPaged(wallet.getId().getValue(), type, page, size);

        List<WalletTransactionDto> dtos = pagedResult.getData().stream()
                .map(t -> WalletTransactionDto.builder()
                        .amount(t.getAmount().getAmount())
                        .transactionType(t.getTransactionType())
                        .referenceId(t.getReferenceId())
                        .createdAt(t.getCreatedAt())
                        .build())
                .collect(Collectors.toList());

        return WalletHistoryResponse.builder()
                .ownerId(ownerId)
                .content(dtos)
                .page(pagedResult.getPageNumber())
                .size(pagedResult.getPageSize())
                .totalElements(pagedResult.getTotalElements())
                .totalPages(pagedResult.getTotalPages())
                .last(pagedResult.getPageNumber() >= pagedResult.getTotalPages() - 1)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public RestaurantFinancialSummaryResponse getFinancialSummary(UUID ownerId) {
        BigDecimal withdrawableBalance = walletRepository.findByOwnerId(ownerId)
                .map(w -> w.getBalance().getAmount())
                .orElse(BigDecimal.ZERO);

        ZonedDateTime startOfDay = ZonedDateTime.now(ZoneId.of(UTC))
                .withHour(0).withMinute(0).withSecond(0).withNano(0);

        OrderEscrowSummaryProjection projection = orderEscrowRepository
                .getEscrowSummary(ownerId, startOfDay.toOffsetDateTime());

        BigDecimal dailySettled = projection != null && projection.getDailySettledRevenue() != null
                ? projection.getDailySettledRevenue()
                : BigDecimal.ZERO;

        BigDecimal pendingPayout = projection != null && projection.getPendingPayout() != null
                ? projection.getPendingPayout()
                : BigDecimal.ZERO;

        BigDecimal dailyRefunded = projection != null && projection.getDailyRefundedAmount() != null
                ? projection.getDailyRefundedAmount()
                : BigDecimal.ZERO;

        return RestaurantFinancialSummaryResponse.builder()
                .restaurantId(ownerId)
                .withdrawableBalance(withdrawableBalance)
                .dailySettledRevenue(dailySettled)
                .pendingPayout(pendingPayout)
                .dailyRefundedAmount(dailyRefunded)
                .build();
    }

    private Wallet getWallet(UUID ownerId) {
        Optional<Wallet> wallet = walletRepository.findByOwnerId(ownerId);
        if (wallet.isEmpty()) {
            throw new PaymentApplicationServiceException("Wallet not found for owner id: " + ownerId);
        }
        return wallet.get();
    }

    private Wallet getWalletWithLock(UUID ownerId) {
        Optional<Wallet> wallet = walletRepository.findByOwnerIdWithLock(ownerId);
        if (wallet.isEmpty()) {
            throw new PaymentApplicationServiceException("Wallet not found for owner id: " + ownerId);
        }
        return wallet.get();
    }
}
