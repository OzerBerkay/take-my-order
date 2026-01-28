package com.berkay.payment.service.domain.mapper;

import com.berkay.payment.service.domain.dto.create.CreditOperationCommand;
import com.berkay.payment.service.domain.dto.create.CreditOperationRequest;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class PaymentRequestMapper {

    public CreditOperationCommand creditOperationRequestToCreditOperationCommand(UUID customerId, CreditOperationRequest creditOperationRequest) {
        return CreditOperationCommand.builder()
                .customerId(customerId)
                .amount(creditOperationRequest.getAmount())
                .transactionType(creditOperationRequest.getTransactionType())
                .build();
    }
}
