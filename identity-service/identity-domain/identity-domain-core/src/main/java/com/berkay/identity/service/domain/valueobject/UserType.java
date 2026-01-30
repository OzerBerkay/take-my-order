package com.berkay.identity.service.domain.valueobject;

public enum UserType {
    CUSTOMER,           // Son kullanıcı (B2C). Tekil role sahiptir. Direkt aktif olur.
    MERCHANT,           // Restoran/Mağaza sahibi (B2B). Tekil role sahiptir. Onay (Approval) süreci vardır.
    INTERNAL            // Şirket içi personel (Backoffice). Çoklu role sahip olabilir.
}