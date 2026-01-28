package com.berkay.payment.service.domain;

import com.berkay.payment.service.domain.dto.create.CreditOperationCommand;
import com.berkay.payment.service.domain.dto.create.CreditOperationResponse;
import com.berkay.payment.service.domain.ports.input.service.PaymentApplicationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Slf4j
@Validated
@Service
public class PaymentApplicationServiceImpl implements PaymentApplicationService {

    private final CreditOperationCommandHandler creditOperationCommandHandler;

    public PaymentApplicationServiceImpl(CreditOperationCommandHandler creditOperationCommandHandler) {
        this.creditOperationCommandHandler = creditOperationCommandHandler;
    }

    @Override
    public CreditOperationResponse processCreditOperation(CreditOperationCommand creditOperationCommand) {
        return creditOperationCommandHandler.processCreditOperation(creditOperationCommand);
    }
}
