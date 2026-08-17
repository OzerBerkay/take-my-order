package com.berkay.payment.service.domain;

import com.berkay.domain.valueobject.PaymentStatus;
import com.berkay.payment.service.domain.entity.Payment;
import com.berkay.payment.service.domain.entity.Wallet;
import com.berkay.payment.service.domain.entity.WalletTransaction;
import com.berkay.payment.service.domain.event.PaymentCancelledEvent;
import com.berkay.payment.service.domain.event.PaymentCompletedEvent;
import com.berkay.payment.service.domain.event.PaymentEvent;
import com.berkay.payment.service.domain.event.PaymentFailedEvent;
import com.berkay.payment.service.domain.exception.PaymentDomainException;
import com.berkay.payment.service.domain.valueobject.WalletTransactionType;
import lombok.extern.slf4j.Slf4j;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import static com.berkay.domain.DomainConstants.UTC;

@Slf4j
public class PaymentDomainServiceImpl implements PaymentDomainService {

    @Override
    public PaymentEvent validateAndInitiatePayment(Payment payment,
                                                   Wallet wallet,
                                                   List<String> failureMessages) {
        payment.validatePayment(failureMessages);
        payment.initializePayment();
        
        try {
            wallet.subtractBalance(payment.getPrice());
        } catch (PaymentDomainException e) {
            failureMessages.add(e.getMessage());
            log.error("Customer with id: {} doesn't have enough credit for payment!", payment.getCustomerId().getValue());
        }

        if (failureMessages.isEmpty()) {
            WalletTransaction transaction = createWalletTransaction(payment, wallet, WalletTransactionType.PAYMENT);
            log.info("Payment is initiated for order id: {}", payment.getOrderId().getValue());
            payment.updateStatus(PaymentStatus.COMPLETED);
            return new PaymentCompletedEvent(payment, ZonedDateTime.now(ZoneId.of(UTC)), transaction, wallet);
        } else {
            log.info("Payment initiation is failed for order id: {}", payment.getOrderId().getValue());
            payment.updateStatus(PaymentStatus.FAILED);
            return new PaymentFailedEvent(payment, ZonedDateTime.now(ZoneId.of(UTC)), failureMessages, wallet);
        }
    }

    @Override
    public PaymentEvent validateAndCancelPayment(Payment payment,
                                                 Wallet wallet,
                                                 List<String> failureMessages) {
        payment.validatePaymentForCancellation(failureMessages);
        
        if (failureMessages.isEmpty()) {
            wallet.addBalance(payment.getPrice());
            WalletTransaction transaction = createWalletTransaction(payment, wallet, WalletTransactionType.REFUND);

            log.info("Payment is cancelled for order id: {}", payment.getOrderId().getValue());
            payment.updateStatus(PaymentStatus.CANCELLED);
            return new PaymentCancelledEvent(payment, ZonedDateTime.now(ZoneId.of(UTC)), transaction, wallet);
        } else {
            log.info("Payment cancellation is failed for order id: {}", payment.getOrderId().getValue());
            payment.updateStatus(PaymentStatus.FAILED);
            return new PaymentFailedEvent(payment, ZonedDateTime.now(ZoneId.of(UTC)), failureMessages, wallet);
        }
    }

    private WalletTransaction createWalletTransaction(Payment payment, Wallet wallet, WalletTransactionType type) {
        return WalletTransaction.builder()
                .walletId(wallet.getId())
                .amount(payment.getPrice())
                .transactionType(type)
                .referenceId(payment.getOrderId().getValue().toString())
                .createdAt(ZonedDateTime.now(ZoneId.of(UTC)))
                .build();
    }
}
