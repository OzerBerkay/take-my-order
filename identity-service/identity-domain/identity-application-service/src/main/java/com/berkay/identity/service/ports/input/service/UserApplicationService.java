package com.berkay.identity.service.ports.input.service;

import com.berkay.identity.service.dto.command.*;

public interface UserApplicationService {

    // Müşteri Kaydı
    CreateUserResponse registerCustomer(RegisterCustomerCommand command);

    // Merchant Kaydı
    CreateUserResponse registerMerchant(RegisterMerchantCommand command);

    // Admin Tarafından Kullanıcı Oluşturma
    CreateUserResponse registerInternalUser(RegisterInternalUserCommand command);

    // Doğrulama İşlemleri
    void verifyEmail(VerifyEmailCommand command);

    void verifyPhone(VerifyPhoneCommand command);

}