package com.berkay.payment.service.domain;

import com.berkay.domain.valueobject.PaymentStatus;
import com.berkay.payment.service.domain.entity.CreditEntry;
import com.berkay.payment.service.domain.entity.CreditHistory;
import com.berkay.payment.service.domain.entity.Payment;
import com.berkay.payment.service.domain.event.PaymentCancelledEvent;
import com.berkay.payment.service.domain.event.PaymentCompletedEvent;
import com.berkay.payment.service.domain.event.PaymentEvent;
import com.berkay.payment.service.domain.event.PaymentFailedEvent;
import com.berkay.payment.service.domain.exception.PaymentDomainException;
import com.berkay.payment.service.domain.valueobject.CreditHistoryId;
import com.berkay.payment.service.domain.valueobject.TransactionType;
import lombok.extern.slf4j.Slf4j;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import static com.berkay.domain.DomainConstants.UTC;

@Slf4j
public class PaymentDomainServiceImpl implements PaymentDomainService {

    @Override
    public void validateAndUpdateCreditEntry(CreditEntry creditEntry,
                                             CreditHistory newCreditHistory) {

        // Transaction Tipine Göre İşlem Yap (CREDIT vs DEBIT)
        if (TransactionType.DEBIT == newCreditHistory.getTransactionType()) {
            // DEBIT (Para Çekme)
            // Bakiye Kontrolü
            if (newCreditHistory.getAmount().isGreaterThan(creditEntry.getTotalCreditAmount())) {
                log.error("Customer with id: {} doesn't have enough credit for withdrawal!",
                        creditEntry.getCustomerId().getValue());

                throw new PaymentDomainException("Customer with id=" + creditEntry.getCustomerId().getValue()
                        + " doesn't have enough credit for withdrawal!");
            }

            // Bakiyeden Düş
            creditEntry.subtractCreditAmount(newCreditHistory.getAmount());

        } else if (TransactionType.CREDIT == newCreditHistory.getTransactionType()) {
            // CREDIT (Para Yükleme)
            creditEntry.addCreditAmount(newCreditHistory.getAmount());
        }

        log.info("Credit entry updated for customer: {}. Type: {}, Amount: {}, New Balance: {}",
                creditEntry.getCustomerId().getValue(),
                newCreditHistory.getTransactionType(),
                newCreditHistory.getAmount().getAmount(),
                creditEntry.getTotalCreditAmount().getAmount());
    }

    @Override
    public PaymentEvent validateAndInitiatePayment(Payment payment,
                                                   CreditEntry creditEntry,
                                                   List<String> failureMessages) {
        payment.validatePayment(failureMessages);
        payment.initializePayment();
        validateCreditEntry(payment, creditEntry, failureMessages);
        subtractCreditEntry(payment, creditEntry);

        if (failureMessages.isEmpty()) {
            CreditHistory history = createCreditHistory(payment, TransactionType.DEBIT);

            log.info("Payment is initiated for order id: {}", payment.getOrderId().getValue());
            payment.updateStatus(PaymentStatus.COMPLETED);
            return new PaymentCompletedEvent(payment, ZonedDateTime.now(ZoneId.of(UTC)), history);
        } else {
            log.info("Payment initiation is failed for order id: {}", payment.getOrderId().getValue());
            payment.updateStatus(PaymentStatus.FAILED);
            return new PaymentFailedEvent(payment, ZonedDateTime.now(ZoneId.of(UTC)), failureMessages);
        }
    }

    @Override
    public PaymentEvent validateAndCancelPayment(Payment payment,
                                                 CreditEntry creditEntry,
                                                 List<String> failureMessages) {
        payment.validatePaymentForCancellation(failureMessages);
        addCreditEntry(payment, creditEntry);

        if (failureMessages.isEmpty()) {
            CreditHistory creditHistory = createCreditHistory(payment, TransactionType.CREDIT);

            log.info("Payment is cancelled for order id: {}", payment.getOrderId().getValue());
            payment.updateStatus(PaymentStatus.CANCELLED);
            return new PaymentCancelledEvent(payment, ZonedDateTime.now(ZoneId.of(UTC)), creditHistory);
        } else {
            log.info("Payment cancellation is failed for order id: {}", payment.getOrderId().getValue());
            payment.updateStatus(PaymentStatus.FAILED);
            return new PaymentFailedEvent(payment, ZonedDateTime.now(ZoneId.of(UTC)), failureMessages);
        }
    }

    private void validateCreditEntry(Payment payment, CreditEntry creditEntry, List<String> failureMessages) {
        if (payment.getPrice().isGreaterThan(creditEntry.getTotalCreditAmount())) {
            log.error("Customer with id: {} doesn't have enough credit for payment!",
                    payment.getCustomerId().getValue());
            failureMessages.add("Customer with id=" + payment.getCustomerId().getValue()
                    + " doesn't have enough credit for payment!");
        }
    }

    private void subtractCreditEntry(Payment payment, CreditEntry creditEntry) {
        creditEntry.subtractCreditAmount(payment.getPrice());
    }

    private CreditHistory createCreditHistory(Payment payment,
                                     TransactionType transactionType) {
        return CreditHistory.builder()
                .creditHistoryId(new CreditHistoryId(UUID.randomUUID()))
                .customerId(payment.getCustomerId())
                .amount(payment.getPrice())
                .transactionType(transactionType)
                .createdAt(ZonedDateTime.now(ZoneId.of(UTC)))
                .build();
    }

    private void addCreditEntry(Payment payment, CreditEntry creditEntry) {
        creditEntry.addCreditAmount(payment.getPrice());
    }
}
