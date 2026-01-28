package com.berkay.payment.service.domain.ports.input.service;

import com.berkay.payment.service.domain.dto.create.CreditOperationCommand;
import com.berkay.payment.service.domain.dto.create.CreditOperationResponse;
import com.berkay.payment.service.domain.dto.query.CreditBalanceResponse;
import com.berkay.payment.service.domain.dto.query.GetCreditBalanceQuery;
import jakarta.validation.Valid;

public interface PaymentApplicationService {
    CreditOperationResponse processCreditOperation(@Valid CreditOperationCommand creditOperationCommand);

    CreditBalanceResponse getCreditBalance(GetCreditBalanceQuery query);
}
