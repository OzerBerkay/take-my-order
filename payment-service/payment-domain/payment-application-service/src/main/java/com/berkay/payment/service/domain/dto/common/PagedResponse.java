package com.berkay.payment.service.domain.dto.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PagedResponse<T> {
    private List<T> content;      // Asıl veri listesi (Örn: List<CreditHistoryResponse>)
    private int pageNumber;       // Mevcut sayfa no (0'dan başlar)
    private int pageSize;         // Sayfa boyutu
    private long totalElements;   // DB'deki toplam kayıt sayısı
    private int totalPages;       // Toplam sayfa sayısı
    private boolean last;         // Son sayfada mıyız?
}
