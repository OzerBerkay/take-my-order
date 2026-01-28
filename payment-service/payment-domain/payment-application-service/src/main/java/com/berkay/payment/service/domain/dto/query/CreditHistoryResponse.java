package com.berkay.payment.service.domain.dto.query;

import com.berkay.payment.service.domain.valueobject.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class CreditHistoryResponse {

    // İşlemin benzersiz ID'si (Frontend belki detayına gitmek ister)
    private final UUID creditHistoryId;

    // İşlem Tipi (DEBIT: Harcama, CREDIT: Yükleme)
    private final TransactionType transactionType;

    // İşlem Tutarı
    private final BigDecimal amount;

    // İşlem Tarihi (ZonedDateTime kullanmak her zaman daha güvenlidir)
    private final ZonedDateTime createdAt;
}
