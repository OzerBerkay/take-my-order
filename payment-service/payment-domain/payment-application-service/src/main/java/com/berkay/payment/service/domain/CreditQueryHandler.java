package com.berkay.payment.service.domain;

import com.berkay.domain.valueobject.CustomerId;
import com.berkay.payment.service.domain.dto.query.CreditBalanceResponse;
import com.berkay.payment.service.domain.dto.query.GetCreditBalanceQuery;
import com.berkay.payment.service.domain.exception.PaymentNotFoundException;
import com.berkay.payment.service.domain.mapper.PaymentDataMapper;
import com.berkay.payment.service.domain.ports.output.repository.CreditEntryRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
public class CreditQueryHandler {

    private final CreditEntryRepository creditEntryRepository;
    private final PaymentDataMapper paymentDataMapper;

    public CreditQueryHandler(CreditEntryRepository creditEntryRepository,
                              PaymentDataMapper paymentDataMapper) {
        this.creditEntryRepository = creditEntryRepository;
        this.paymentDataMapper = paymentDataMapper;
    }

    @Transactional(readOnly = true) // Sadece okuma, kilit yok!
    public CreditBalanceResponse getCreditBalance(GetCreditBalanceQuery query) {
        log.info("Fetching balance for customer: {}", query.customerId());

        return creditEntryRepository.findByCustomerId(new CustomerId(query.customerId()))
                .map(paymentDataMapper::creditEntryTocCreditBalanceResponse)
                .orElseThrow(() -> {
                    log.error("Credit entry not found for customer: {}", query.customerId());
                    return new PaymentNotFoundException("Credit entry not found!");
                });
    }
}
