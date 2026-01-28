package com.berkay.payment.service.domain;

import com.berkay.domain.valueobject.CustomerId;
import com.berkay.domain.valueobject.Money;
import com.berkay.payment.service.domain.dto.create.CreditOperationCommand;
import com.berkay.payment.service.domain.dto.create.CreditOperationResponse;
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

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Component
public class CreditOperationCommandHandler {

    private final PaymentDomainService paymentDomainService;
    private final CreditEntryRepository creditEntryRepository;
    private final CreditHistoryRepository creditHistoryRepository;
    private final PaymentDataMapper paymentDataMapper;

    public CreditOperationCommandHandler(PaymentDomainService paymentDomainService,
                                         CreditEntryRepository creditEntryRepository,
                                         CreditHistoryRepository creditHistoryRepository,
                                         PaymentDataMapper paymentDataMapper) {
        this.paymentDomainService = paymentDomainService;
        this.creditEntryRepository = creditEntryRepository;
        this.creditHistoryRepository = creditHistoryRepository;
        this.paymentDataMapper = paymentDataMapper;
    }

    @Transactional
    public CreditOperationResponse processCreditOperation(CreditOperationCommand command) {
        CustomerId customerId = new CustomerId(command.getCustomerId());

        // Cüzdanı (CreditEntry) Bul veya Oluştur
        CreditEntry creditEntry = getOrCreateCreditEntry(customerId, command.getTransactionType());

        // Command -> Entity (History) Dönüşümü
        // Bu yeni history nesnesi, işlemin miktarını ve tipini (DEBIT/CREDIT) taşıyor.
        CreditHistory newCreditHistory = paymentDataMapper.creditHistoryFromCreditOperationCommand(command);

        // Domain Logic Çağrısı (Validasyon ve Güncelleme)
        // Yetersiz bakiye durumunda Domain Exception fırlatır.
        paymentDomainService.validateAndUpdateCreditEntry(creditEntry, newCreditHistory);

        // Veritabanına Kaydet
        creditEntryRepository.save(creditEntry);
        creditHistoryRepository.save(newCreditHistory);

        log.info("Credit operation processed for customer: {}, new balance: {}",
                customerId.getValue(), creditEntry.getTotalCreditAmount().getAmount());

        return paymentDataMapper.creditOperationResponseFromCreditEntry(creditEntry);
    }

    private CreditEntry getOrCreateCreditEntry(CustomerId customerId, TransactionType transactionType) {
        /*
         * PESSIMISTIC LOCKING STRATEJİSİ:
         * Burada "SELECT FOR UPDATE" (Pessimistic Write) kullanarak, aynı müşteri için
         * eş zamanlı (concurrent) gelen bakiye yükleme/çekme isteklerinde Race Condition
         * oluşmasını ve veri tutarsızlığını (Lost Update) engelliyoruz.
         *
         * DEADLOCK GÜVENLİĞİ:
         * Ödeme akışında (persistPayment) ve manuel operasyonlarda (burada) sadece
         * tek bir kaynak (CreditEntry satırı) kilitlenmektedir. Çapraz kaynak bağımlılığı
         * (Örn: A'yı tutup B'yi bekleme durumu) olmadığı ve kilitler işlem (transaction)
         * bitiminde serbest bırakıldığı için Deadlock riski yoktur.
         */
        Optional<CreditEntry> creditEntryOptional = creditEntryRepository.findByCustomerIdWithLock(customerId);

        if (creditEntryOptional.isPresent()) {
            return creditEntryOptional.get();
        }

        // Cüzdan yoksa (Burada kilitlenecek bir satır yok demektir, create edeceğiz)
        if (TransactionType.CREDIT == transactionType) {
            // Para YÜKLENİYORSA cüzdanı oluştur
            // Not: Burada henüz DB'de satır olmadığı için Lock çalışmaz ama
            // Unique Constraint (customerId) sayesinde iki kişi aynı anda yaratmaya çalışırsa biri hata alır.
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
