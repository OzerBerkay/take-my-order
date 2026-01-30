package com.berkay.identity.service.domain;

import com.berkay.identity.service.domain.entity.User;
import com.berkay.identity.service.domain.event.UserCreatedEvent;
import com.berkay.identity.service.domain.event.UserUpdatedEvent;
import com.berkay.identity.service.domain.valueobject.FirstName;
import com.berkay.identity.service.domain.valueobject.LastName;

public interface IdentityDomainService {

    // 1. Müşteri Kaydı Başlatma
    UserCreatedEvent initiateCustomer(User user);

    // 2. Restoran/Merchant Başvurusu Başlatma
    UserCreatedEvent initiateMerchant(User user);

    // 3. İç Personel (Admin) Kaydı Başlatma
    UserCreatedEvent initiateInternalUser(User user);

    // 4. Profil Güncelleme
    UserUpdatedEvent validateAndUpdateProfile(User user,
                                              FirstName firstName,
                                              LastName lastName,
                                              String imageUrl);
}