package com.berkay.payment.service.domain;

import com.berkay.domain.valueobject.CustomerId;
import com.berkay.payment.service.domain.dto.common.PagedResponse;
import com.berkay.payment.service.domain.dto.query.CreditBalanceResponse;
import com.berkay.payment.service.domain.dto.query.CreditHistoryResponse;
import com.berkay.payment.service.domain.dto.query.GetCreditBalanceQuery;
import com.berkay.payment.service.domain.dto.query.GetCreditHistoryQuery;
import com.berkay.payment.service.domain.entity.CreditHistory;
import com.berkay.payment.service.domain.exception.PaymentNotFoundException;
import com.berkay.payment.service.domain.mapper.PaymentDataMapper;
import com.berkay.payment.service.domain.ports.output.repository.CreditEntryRepository;
import com.berkay.payment.service.domain.ports.output.repository.CreditHistoryRepository;
import com.berkay.payment.service.domain.valueobject.DomainPagedResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class CreditQueryHandler {

    private final CreditEntryRepository creditEntryRepository;
    private final CreditHistoryRepository creditHistoryRepository;
    private final PaymentDataMapper paymentDataMapper;

    public CreditQueryHandler(CreditEntryRepository creditEntryRepository,
                              CreditHistoryRepository creditHistoryRepository,
                              PaymentDataMapper paymentDataMapper) {
        this.creditEntryRepository = creditEntryRepository;
        this.creditHistoryRepository = creditHistoryRepository;
        this.paymentDataMapper = paymentDataMapper;
    }

    @Transactional(readOnly = true) // Sadece okuma, kilit yok!
    public CreditBalanceResponse getCreditBalance(GetCreditBalanceQuery query) {
        log.info("Fetching balance for customer: {}", query.customerId());

        return creditEntryRepository.findByCustomerId(new CustomerId(query.customerId()))
                .map(paymentDataMapper::creditEntryTocCreditBalanceResponse)
                .orElseThrow(() -> {
                    log.error("Credit entry not found for customer: {}", query.customerId());
                    return new PaymentNotFoundException("Could not find credit entry for customer: " + query.customerId());
                });
    }

    @Transactional(readOnly = true)
    public PagedResponse<CreditHistoryResponse> getCreditHistory(GetCreditHistoryQuery query) {
        log.info("Fetching credit history for customer: {}, page: {}", query.customerId(), query.page());

        // Domain PagedResult çekiliyor (İsimlendirme: findByCustomerIdPageable)
        DomainPagedResult<CreditHistory> domainResult = creditHistoryRepository.findByCustomerIdPageable(
                new CustomerId(query.customerId()),
                query.page(),
                query.size()
        );

        // Entity -> Response DTO dönüşümü
        List<CreditHistoryResponse> responseList = domainResult.getData().stream()
                .map(paymentDataMapper::creditHistoryResponseFromCreditHistory)
                .collect(Collectors.toList());

        // Generic PagedResponse oluşturma
        return PagedResponse.<CreditHistoryResponse>builder()
                .content(responseList)
                .pageNumber(domainResult.getPageNumber())
                .pageSize(domainResult.getPageSize())
                .totalElements(domainResult.getTotalElements())
                .totalPages(domainResult.getTotalPages())
                .last(domainResult.getPageNumber() == domainResult.getTotalPages() - 1)
                .build();
    }
}
