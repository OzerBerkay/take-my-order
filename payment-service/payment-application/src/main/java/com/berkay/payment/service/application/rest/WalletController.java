package com.berkay.payment.service.application.rest;

import com.berkay.payment.service.domain.WalletApplicationService;
import com.berkay.payment.service.domain.dto.wallet.WalletBalanceResponse;
import com.berkay.payment.service.domain.dto.wallet.WalletDepositCommand;
import com.berkay.payment.service.domain.dto.wallet.WalletHistoryResponse;
import com.berkay.payment.service.domain.dto.wallet.WalletWithdrawCommand;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping(value = "/wallets", produces = "application/vnd.api.v1+json")
public class WalletController {

    private final WalletApplicationService walletApplicationService;

    public WalletController(WalletApplicationService walletApplicationService) {
        this.walletApplicationService = walletApplicationService;
    }

    @PostMapping("/{ownerId}/deposit")
    @PreAuthorize("@paymentAuthService.hasPermission(authentication, 'can_manage_payment', #ownerId)")
    public ResponseEntity<WalletBalanceResponse> deposit(@PathVariable("ownerId") UUID ownerId,
                                        @RequestHeader("Idempotency-Key") String idempotencyKey,
                                        @RequestBody WalletDepositCommand command) {
        log.info("Received deposit request for owner id: {}", ownerId);
        command.setOwnerId(ownerId);
        command.setIdempotencyKey(idempotencyKey);
        WalletBalanceResponse response = walletApplicationService.deposit(command);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{ownerId}/withdraw")
    @PreAuthorize("@paymentAuthService.hasPermission(authentication, 'can_manage_payment', #ownerId)")
    public ResponseEntity<WalletBalanceResponse> withdraw(@PathVariable("ownerId") UUID ownerId,
                                         @RequestHeader("Idempotency-Key") String idempotencyKey,
                                         @RequestBody WalletWithdrawCommand command) {
        log.info("Received withdraw request for owner id: {}", ownerId);
        command.setOwnerId(ownerId);
        command.setIdempotencyKey(idempotencyKey);
        WalletBalanceResponse response = walletApplicationService.withdraw(command);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{ownerId}/balance")
    @PreAuthorize("@paymentAuthService.hasPermission(authentication, 'can_read_payment', #ownerId)")
    public ResponseEntity<WalletBalanceResponse> getBalance(@PathVariable("ownerId") UUID ownerId) {
        log.info("Received get balance request for owner id: {}", ownerId);
        WalletBalanceResponse response = walletApplicationService.getBalance(ownerId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{ownerId}/history")
    @PreAuthorize("@paymentAuthService.hasPermission(authentication, 'can_read_payment', #ownerId)")
    public ResponseEntity<WalletHistoryResponse> getHistory(@PathVariable("ownerId") UUID ownerId) {
        log.info("Received get history request for owner id: {}", ownerId);
        WalletHistoryResponse response = walletApplicationService.getHistory(ownerId);
        return ResponseEntity.ok(response);
    }
}
