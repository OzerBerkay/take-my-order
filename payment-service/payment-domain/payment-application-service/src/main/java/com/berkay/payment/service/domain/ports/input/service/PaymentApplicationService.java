package com.berkay.payment.service.domain.ports.input.service;

import com.berkay.payment.service.domain.dto.create.CreditOperationCommand;
import com.berkay.payment.service.domain.dto.create.CreditOperationResponse;
import jakarta.validation.Valid;

public interface PaymentApplicationService {
    CreditOperationResponse processCreditOperation(@Valid CreditOperationCommand creditOperationCommand);
}
