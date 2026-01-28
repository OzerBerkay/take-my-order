package com.berkay.payment.service.dataaccess.credithistory.repository;

import com.berkay.payment.service.dataaccess.credithistory.entity.CreditHistoryEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CreditHistoryJpaRepository extends JpaRepository<CreditHistoryEntity, UUID> {

    // "Pageable" parametresi sayesinde limit, offset ve sort işlemlerini otomatik yapar.
    Page<CreditHistoryEntity> findByCustomerId(UUID customerId, Pageable pageable);
}
