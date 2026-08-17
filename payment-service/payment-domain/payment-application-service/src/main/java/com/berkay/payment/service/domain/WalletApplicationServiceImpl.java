package com.berkay.payment.service.domain;

import com.berkay.domain.valueobject.Money;
import com.berkay.payment.service.domain.dto.wallet.WalletBalanceResponse;
import com.berkay.payment.service.domain.dto.wallet.WalletDepositCommand;
import com.berkay.payment.service.domain.dto.wallet.WalletHistoryResponse;
import com.berkay.payment.service.domain.dto.wallet.WalletTransactionDto;
import com.berkay.payment.service.domain.dto.wallet.WalletWithdrawCommand;
import com.berkay.payment.service.domain.entity.Wallet;
import com.berkay.payment.service.domain.entity.WalletTransaction;
import com.berkay.payment.service.domain.exception.PaymentApplicationServiceException;
import com.berkay.payment.service.domain.ports.output.repository.WalletRepository;
import com.berkay.payment.service.domain.ports.output.repository.WalletTransactionRepository;
import com.berkay.payment.service.domain.valueobject.WalletTransactionType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

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

    public WalletApplicationServiceImpl(WalletRepository walletRepository,
                                        WalletTransactionRepository walletTransactionRepository) {
        this.walletRepository = walletRepository;
        this.walletTransactionRepository = walletTransactionRepository;
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
        Wallet wallet = getWallet(ownerId);
        List<WalletTransaction> transactions = walletTransactionRepository.findByWalletId(wallet.getId().getValue());
        
        List<WalletTransactionDto> dtos = transactions.stream()
                .map(t -> WalletTransactionDto.builder()
                        .amount(t.getAmount().getAmount())
                        .transactionType(t.getTransactionType())
                        .referenceId(t.getReferenceId())
                        .createdAt(t.getCreatedAt())
                        .build())
                .collect(Collectors.toList());

        return WalletHistoryResponse.builder()
                .ownerId(ownerId)
                .transactions(dtos)
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
