package com.berkay.domain.valueobject;

/*
OrderStatus yerine neden PaymentOrderStatus kullanıldı, OrderStatus neden yetmedi?
OrderStatus.PENDING: "Sipariş verdim, henüz kimse onaylamadı, ödeme alınmadı."
PaymentOrderStatus.PENDING: "Ödeme servisine istek attım, ondan cevap bekliyorum."

Siparişin durumu CANCELLING (İptal ediliyor) olabilir ama Ödeme Servisi'ne giden mesajın durumu hala PENDING olabilir.
Ödeme servisi, siparişin o karmaşık durumlarını bilmek zorunda kalmamalı. Ona sadece net bir emir gitmeli:
PENDING: "Para çekme işlemini başlat." (Sipariş oluşurken gönderilir).
CANCELLED: "Para iadesi yap / İşlemi iptal et." (Sipariş iptal edilirken gönderilir).
*/
public enum PaymentOrderStatus {
    PENDING, CANCELLED
}
