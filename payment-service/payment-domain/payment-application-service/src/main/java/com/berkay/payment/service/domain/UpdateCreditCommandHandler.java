package com.berkay.payment.service.domain;

import com.berkay.domain.valueobject.CustomerId;
import com.berkay.domain.valueobject.Money;
import com.berkay.payment.service.domain.dto.UpdateCreditCommand;
import com.berkay.payment.service.domain.dto.UpdateCreditResponse;
import com.berkay.payment.service.domain.entity.CreditEntry;
import com.berkay.payment.service.domain.entity.CreditHistory;
import com.berkay.payment.service.domain.exception.PaymentApplicationServiceException;
import com.berkay.payment.service.domain.mapper.PaymentDataMapper;
import com.berkay.payment.service.domain.ports.output.repository.CreditEntryRepository;
import com.berkay.payment.service.domain.ports.output.repository.CreditHistoryRepository;
import com.berkay.payment.service.domain.valueobject.CreditEntryId;
import com.berkay.payment.service.domain.valueobject.TransactionType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Component
public class UpdateCreditCommandHandler {

    private final PaymentDomainService paymentDomainService;
    private final CreditEntryRepository creditEntryRepository;
    private final CreditHistoryRepository creditHistoryRepository;
    private final PaymentDataMapper paymentDataMapper;

    public UpdateCreditCommandHandler(PaymentDomainService paymentDomainService,
                                      CreditEntryRepository creditEntryRepository,
                                      CreditHistoryRepository creditHistoryRepository,
                                      PaymentDataMapper paymentDataMapper) {
        this.paymentDomainService = paymentDomainService;
        this.creditEntryRepository = creditEntryRepository;
        this.creditHistoryRepository = creditHistoryRepository;
        this.paymentDataMapper = paymentDataMapper;
    }

    @Transactional
    public UpdateCreditResponse updateCredit(UpdateCreditCommand command) {
        CustomerId customerId = new CustomerId(command.getCustomerId());

        // Cüzdanı (CreditEntry) Bul veya Oluştur
        CreditEntry creditEntry = getOrCreateCreditEntry(customerId, command.getTransactionType());

        // Geçmişi (History) Bul
        List<CreditHistory> creditHistories = creditHistoryRepository.findByCustomerId(customerId)
                .orElse(new ArrayList<>());

        // Command -> Entity (History) Dönüşümü
        CreditHistory newCreditHistory = paymentDataMapper.creditHistoryFromUpdateCreditCommand(command);

        // Domain Logic Çağrısı (Validasyon ve Güncelleme)
        // Yetersiz bakiye durumunda Domain Exception fırlatır.
        paymentDomainService.validateAndUpdateCreditEntry(creditEntry, creditHistories, newCreditHistory);

        // Veritabanına Kaydet
        creditEntryRepository.save(creditEntry);
        creditHistoryRepository.save(newCreditHistory);

        log.info("Credit updated for customer: {}, new balance: {}",
                customerId.getValue(), creditEntry.getTotalCreditAmount().getAmount());

        return paymentDataMapper.updateCreditResponseFromCreditEntry(creditEntry);
    }

    private CreditEntry getOrCreateCreditEntry(CustomerId customerId, TransactionType transactionType) {
        Optional<CreditEntry> creditEntryOptional = creditEntryRepository.findByCustomerId(customerId);

        if (creditEntryOptional.isPresent()) {
            return creditEntryOptional.get();
        }

        // Cüzdan yoksa
        if (TransactionType.CREDIT == transactionType) {
            // Para YÜKLENİYORSA cüzdanı oluştur
            return CreditEntry.builder()
                    .creditEntryId(new CreditEntryId(UUID.randomUUID()))
                    .customerId(customerId)
                    .totalCreditAmount(Money.ZERO)
                    .build();
        } else {
            // Para ÇEKİLİYORSA ve cüzdan yoksa HATA
            log.error("Could not find credit entry for customer: {}", customerId.getValue());
            throw new PaymentApplicationServiceException("Could not find credit entry for customer: " +
                    customerId.getValue());
        }
    }
}
