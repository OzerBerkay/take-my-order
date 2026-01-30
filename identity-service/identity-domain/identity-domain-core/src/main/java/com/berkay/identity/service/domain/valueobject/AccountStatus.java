package com.berkay.identity.service.domain.valueobject;

public enum AccountStatus {
    PENDING_VERIFICATION, // Email/SMS onayı bekliyor
    PENDING_APPROVAL,     // (Sadece Merchant için) Backoffice onayı bekliyor
    ACTIVE,               // Her şey tamam, sistemde aktif
    BLOCKED,              // Kural ihlali, geçici blok
    BANNED                // Kalıcı ban
}