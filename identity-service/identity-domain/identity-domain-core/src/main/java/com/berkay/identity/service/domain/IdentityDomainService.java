package com.berkay.identity.service.domain;

import com.berkay.identity.service.domain.entity.User;
import com.berkay.identity.service.domain.event.UserCreatedEvent;
import com.berkay.identity.service.domain.event.UserUpdatedEvent;

public interface IdentityDomainService {

    // Kullanıcıyı validasyonlardan geçirir, initialize eder ve event fırlatır.
    UserCreatedEvent validateAndInitiateUser(User user);

    UserUpdatedEvent validateAndUpdateProfile(User user, String firstName, String lastName, String imageUrl);
}