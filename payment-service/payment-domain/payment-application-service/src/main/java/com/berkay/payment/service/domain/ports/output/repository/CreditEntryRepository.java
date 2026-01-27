package com.berkay.payment.service.domain.ports.output.repository;

import com.berkay.domain.valueobject.CustomerId;
import com.berkay.payment.service.domain.entity.CreditEntry;

import java.util.Optional;

public interface CreditEntryRepository {

    CreditEntry save(CreditEntry creditEntry);

    Optional<CreditEntry> findByCustomerId(CustomerId customerId);

    Optional<CreditEntry> findByCustomerIdWithLock(CustomerId customerId);
}
