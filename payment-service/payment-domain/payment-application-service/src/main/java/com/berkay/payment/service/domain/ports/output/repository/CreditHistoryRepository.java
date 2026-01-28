package com.berkay.payment.service.domain.ports.output.repository;

import com.berkay.domain.valueobject.CustomerId;
import com.berkay.payment.service.domain.entity.CreditHistory;
import com.berkay.payment.service.domain.valueobject.DomainPagedResult;

import java.util.List;

public interface CreditHistoryRepository {

    CreditHistory save(CreditHistory creditHistory);

    List<CreditHistory> findByCustomerId(CustomerId customerId);

    DomainPagedResult<CreditHistory> findByCustomerIdPageable(CustomerId customerId, int page, int size);}
