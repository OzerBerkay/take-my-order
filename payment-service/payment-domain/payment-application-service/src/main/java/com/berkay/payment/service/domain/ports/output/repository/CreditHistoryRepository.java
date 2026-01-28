package com.berkay.payment.service.domain.ports.output.repository;

import com.berkay.domain.valueobject.CustomerId;
import com.berkay.payment.service.domain.entity.CreditHistory;
import com.berkay.payment.service.domain.valueobject.CreditHistoryId;
import com.berkay.payment.service.domain.valueobject.DomainPagedResult;

import java.util.Optional;

public interface CreditHistoryRepository {

    CreditHistory save(CreditHistory creditHistory);

    Optional<CreditHistory> findById(CreditHistoryId id);

    DomainPagedResult<CreditHistory> findPagedCreditHistoriesByCustomerId(CustomerId customerId, int page, int size);}
