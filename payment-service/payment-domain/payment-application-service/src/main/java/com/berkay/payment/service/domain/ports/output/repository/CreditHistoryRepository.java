package com.berkay.payment.service.domain.ports.output.repository;

import com.berkay.domain.valueobject.CustomerId;
import com.berkay.payment.service.domain.entity.CreditHistory;

import java.util.List;
import java.util.Optional;

public interface CreditHistoryRepository {

    CreditHistory save(CreditHistory creditHistory);

    Optional<List<CreditHistory>> findByCustomerId(CustomerId customerId);
}
