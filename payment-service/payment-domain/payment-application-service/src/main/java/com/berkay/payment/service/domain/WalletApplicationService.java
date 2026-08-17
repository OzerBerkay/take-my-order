package com.berkay.payment.service.domain;

import com.berkay.payment.service.domain.dto.wallet.WalletBalanceResponse;
import com.berkay.payment.service.domain.dto.wallet.WalletDepositCommand;
import com.berkay.payment.service.domain.dto.wallet.WalletHistoryResponse;
import com.berkay.payment.service.domain.dto.wallet.WalletWithdrawCommand;
import jakarta.validation.Valid;

import java.util.UUID;

public interface WalletApplicationService {

    WalletBalanceResponse deposit(@Valid WalletDepositCommand command);

    WalletBalanceResponse withdraw(@Valid WalletWithdrawCommand command);

    WalletBalanceResponse getBalance(UUID ownerId);

    WalletHistoryResponse getHistory(UUID ownerId);
}
