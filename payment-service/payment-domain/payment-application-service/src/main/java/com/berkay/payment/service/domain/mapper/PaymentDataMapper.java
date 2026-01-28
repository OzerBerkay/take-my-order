package com.berkay.payment.service.domain.mapper;

import com.berkay.domain.valueobject.CustomerId;
import com.berkay.domain.valueobject.Money;
import com.berkay.domain.valueobject.OrderId;
import com.berkay.payment.service.domain.dto.PaymentRequest;
import com.berkay.payment.service.domain.dto.create.CreditOperationCommand;
import com.berkay.payment.service.domain.dto.create.CreditOperationResponse;
import com.berkay.payment.service.domain.entity.CreditEntry;
import com.berkay.payment.service.domain.entity.CreditHistory;
import com.berkay.payment.service.domain.entity.Payment;
import com.berkay.payment.service.domain.event.PaymentEvent;
import com.berkay.payment.service.domain.outbox.model.OrderEventPayload;
import com.berkay.payment.service.domain.valueobject.CreditHistoryId;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class PaymentDataMapper {

    public Payment paymentRequestModelToPayment(PaymentRequest paymentRequest) {
        return Payment.builder()
                .orderId(new OrderId(UUID.fromString(paymentRequest.getOrderId())))
                .customerId(new CustomerId(UUID.fromString(paymentRequest.getCustomerId())))
                .price(new Money(paymentRequest.getPrice()))
                .build();
    }

    public OrderEventPayload paymentEventToOrderEventPayload(PaymentEvent paymentEvent) {
        return OrderEventPayload.builder()
                .paymentId(paymentEvent.getPayment().getId().getValue().toString())
                .customerId(paymentEvent.getPayment().getCustomerId().getValue().toString())
                .orderId(paymentEvent.getPayment().getOrderId().getValue().toString())
                .price(paymentEvent.getPayment().getPrice().getAmount())
                .createdAt(paymentEvent.getCreatedAt())
                .paymentStatus(paymentEvent.getPayment().getPaymentStatus().name())
                .failureMessages(paymentEvent.getFailureMessages())
                .build();
    }

    public CreditHistory creditHistoryFromCreditOperationCommand(CreditOperationCommand command) {
        return CreditHistory.builder()
                .creditHistoryId(new CreditHistoryId(UUID.randomUUID()))
                .customerId(new CustomerId(command.getCustomerId()))
                .amount(new Money(command.getAmount()))
                .transactionType(command.getTransactionType())
                .build();
    }

    public CreditOperationResponse creditOperationResponseFromCreditEntry(CreditEntry creditEntry) {
        return CreditOperationResponse.builder()
                .customerId(creditEntry.getCustomerId().getValue())
                .newBalance(creditEntry.getTotalCreditAmount().getAmount())
                .build();
    }

}