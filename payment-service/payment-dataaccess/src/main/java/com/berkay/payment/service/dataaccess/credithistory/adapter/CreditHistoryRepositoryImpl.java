package com.berkay.payment.service.dataaccess.credithistory.adapter;

import com.berkay.domain.valueobject.CustomerId;
import com.berkay.payment.service.dataaccess.credithistory.entity.CreditHistoryEntity;
import com.berkay.payment.service.dataaccess.credithistory.mapper.CreditHistoryDataAccessMapper;
import com.berkay.payment.service.dataaccess.credithistory.repository.CreditHistoryJpaRepository;
import com.berkay.payment.service.domain.entity.CreditHistory;
import com.berkay.payment.service.domain.ports.output.repository.CreditHistoryRepository;
import com.berkay.payment.service.domain.valueobject.CreditHistoryId;
import com.berkay.payment.service.domain.valueobject.DomainPagedResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class CreditHistoryRepositoryImpl implements CreditHistoryRepository {

    private final CreditHistoryJpaRepository creditHistoryJpaRepository;
    private final CreditHistoryDataAccessMapper creditHistoryDataAccessMapper;

    public CreditHistoryRepositoryImpl(CreditHistoryJpaRepository creditHistoryJpaRepository,
                                       CreditHistoryDataAccessMapper creditHistoryDataAccessMapper) {
        this.creditHistoryJpaRepository = creditHistoryJpaRepository;
        this.creditHistoryDataAccessMapper = creditHistoryDataAccessMapper;
    }

    @Override
    public CreditHistory save(CreditHistory creditHistory) {
        return creditHistoryDataAccessMapper.creditHistoryEntityToCreditHistory(creditHistoryJpaRepository
                .save(creditHistoryDataAccessMapper.creditHistoryToCreditHistoryEntity(creditHistory)));
    }

    @Override
    public Optional<CreditHistory> findById(CreditHistoryId creditHistoryId) {
        return creditHistoryJpaRepository.findById(creditHistoryId.getValue())
                .map(creditHistoryDataAccessMapper::creditHistoryEntityToCreditHistory);
    }

    @Override
    public DomainPagedResult<CreditHistory> findPagedCreditHistoriesByCustomerId(CustomerId customerId, int page, int size) {

        // 1. Spring Data Pageable oluştur (Data Access detayları burada kalır)
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        // 2. DB'den Spring Page olarak çek
        Page<CreditHistoryEntity> springPage = creditHistoryJpaRepository.findByCustomerId(customerId.getValue(), pageable);

        // 3. Entity Listesini Domain Listesine çevir
        List<CreditHistory> domainList = springPage.getContent().stream()
                .map(creditHistoryDataAccessMapper::creditHistoryEntityToCreditHistory)
                .collect(Collectors.toList());

        // 4. Spring Page'i -> Domain PagedResult'a çevirip dön
        return new DomainPagedResult<>(
                domainList,
                springPage.getNumber(),
                springPage.getSize(),
                springPage.getTotalElements(),
                springPage.getTotalPages()
        );
    }
}
