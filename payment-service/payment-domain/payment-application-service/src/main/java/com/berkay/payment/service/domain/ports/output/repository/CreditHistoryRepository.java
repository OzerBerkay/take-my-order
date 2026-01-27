package com.berkay.payment.service.domain.ports.output.repository;

import com.berkay.domain.valueobject.CustomerId;
import com.berkay.payment.service.domain.entity.CreditHistory;

import java.util.List;

public interface CreditHistoryRepository {

    CreditHistory save(CreditHistory creditHistory);

    List<CreditHistory> findByCustomerId(CustomerId customerId);
}
