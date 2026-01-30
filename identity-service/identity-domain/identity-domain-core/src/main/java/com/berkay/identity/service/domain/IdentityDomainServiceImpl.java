package com.berkay.identity.service.domain;

import com.berkay.identity.service.domain.entity.User;
import com.berkay.identity.service.domain.event.UserCreatedEvent;
import com.berkay.identity.service.domain.event.UserUpdatedEvent;
import com.berkay.identity.service.domain.valueobject.FirstName;
import com.berkay.identity.service.domain.valueobject.LastName;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import static com.berkay.domain.DomainConstants.UTC;

public class IdentityDomainServiceImpl implements IdentityDomainService {

    @Override
    public UserCreatedEvent initiateCustomer(User user) {
        // Müşteri olarak başlat
        user.initializeCustomer();

        // İş kurallarını denetle (Tek rol var mı? Email/Phone var mı?)
        user.validateUser();

        return new UserCreatedEvent(user, ZonedDateTime.now(ZoneId.of(UTC)));
    }

    @Override
    public UserCreatedEvent initiateMerchant(User user) {
        // Merchant olarak başlat
        user.initializeMerchant();

        // İş kurallarını denetle
        user.validateUser();

        // Not: İleride "MerchantCreatedEvent" diye ayrı bir event fırlatmak istenirse burayı değiştiririz.
        // Şimdilik Identity sistemi için UserCreatedEvent yeterli.
        return new UserCreatedEvent(user, ZonedDateTime.now(ZoneId.of(UTC)));
    }

    @Override
    public UserCreatedEvent initiateInternalUser(User user) {
        // Admin olarak başlat
        user.initializeInternalUser();

        // İş kurallarını denetle (En az 1 rol var mı?)
        user.validateUser();

        return new UserCreatedEvent(user, ZonedDateTime.now(ZoneId.of(UTC)));
    }

    @Override
    public UserUpdatedEvent validateAndUpdateProfile(User user,
                                                     FirstName firstName,
                                                     LastName lastName,
                                                     String imageUrl) {
        // Entity üzerindeki update metodunu çağırıyoruz
        user.updateProfile(firstName, lastName, imageUrl);

        // Gerekirse burada ekstra domain validasyonları yapılabilir
        // Örn: Kullanıcı BANLI ise profil güncelleyemez gibi.
        // if (AccountStatus.BANNED.equals(user.getStatus())) throw ...

        return new UserUpdatedEvent(user, ZonedDateTime.now(ZoneId.of(UTC)));
    }
}