package com.berkay.payment.service.domain;

import com.berkay.payment.service.domain.dto.common.PagedResponse;
import com.berkay.payment.service.domain.dto.create.CreditOperationCommand;
import com.berkay.payment.service.domain.dto.create.CreditOperationResponse;
import com.berkay.payment.service.domain.dto.query.CreditBalanceResponse;
import com.berkay.payment.service.domain.dto.query.CreditHistoryResponse;
import com.berkay.payment.service.domain.dto.query.GetCreditBalanceQuery;
import com.berkay.payment.service.domain.dto.query.GetPagedCreditHistoriesByCustomerIdQuery;
import com.berkay.payment.service.domain.ports.input.service.PaymentApplicationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Slf4j
@Validated
@Service
public class PaymentApplicationServiceImpl implements PaymentApplicationService {

    private final CreditOperationCommandHandler creditOperationCommandHandler;
    private final CreditQueryHandler creditQueryHandler;

    public PaymentApplicationServiceImpl(CreditOperationCommandHandler creditOperationCommandHandler,
                                         CreditQueryHandler creditQueryHandler) {
        this.creditOperationCommandHandler = creditOperationCommandHandler;
        this.creditQueryHandler = creditQueryHandler;
    }

    @Override
    public CreditOperationResponse processCreditOperation(CreditOperationCommand creditOperationCommand) {
        return creditOperationCommandHandler.processCreditOperation(creditOperationCommand);
    }

    @Override
    public CreditBalanceResponse getCreditBalance(GetCreditBalanceQuery query) {
        return creditQueryHandler.getCreditBalance(query);
    }

    @Override
    public PagedResponse<CreditHistoryResponse> getPagedCreditHistoriesByCustomerId(GetPagedCreditHistoriesByCustomerIdQuery query) {
        return creditQueryHandler.getPagedCreditHistoriesByCustomerId(query);
    }
}
