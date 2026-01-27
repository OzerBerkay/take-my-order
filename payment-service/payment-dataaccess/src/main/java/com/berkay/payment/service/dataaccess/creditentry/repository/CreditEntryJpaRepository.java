package com.berkay.payment.service.dataaccess.creditentry.repository;

import com.berkay.payment.service.dataaccess.creditentry.entity.CreditEntryEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CreditEntryJpaRepository extends JpaRepository<CreditEntryEntity, UUID> {

    Optional<CreditEntryEntity> findByCustomerId(UUID customerId);

    /**
     * Refactor notu: History check mantığı kaldırıldığı için bakiye güvenliği
     * tamamen bu sorguya emanettir. SELECT FOR UPDATE (Pessimistic Write) kullanarak
     * aynı anda gelen (Race Condition) ödeme isteklerini DB seviyesinde sıraya diziyoruz.
     * Bu sayede "Lost Update" problemini engelliyor ve bakiyenin eksiye düşmeyeceğini
     * (Atomicity) garanti altına alıyoruz.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from CreditEntryEntity c where c.customerId = :customerId")
    Optional<CreditEntryEntity> findByCustomerIdWithLock(@Param("customerId") UUID customerId);

}