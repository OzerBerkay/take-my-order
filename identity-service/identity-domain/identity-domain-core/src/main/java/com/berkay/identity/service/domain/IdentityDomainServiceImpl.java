package com.berkay.identity.service.domain;

import com.berkay.identity.service.domain.entity.User;
import com.berkay.identity.service.domain.event.UserCreatedEvent;
import com.berkay.identity.service.domain.event.UserUpdatedEvent;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import static com.berkay.domain.DomainConstants.UTC;

public class IdentityDomainServiceImpl implements IdentityDomainService {

    @Override
    public UserCreatedEvent validateAndInitiateUser(User user) {
        // 1. Domain kurallarını işlet
        user.validateUser();
        user.initializeUser();

        // 2. Event fırlat (Bu event daha sonra Application katmanında Kafka'ya basılacak)
        return new UserCreatedEvent(user, ZonedDateTime.now(ZoneId.of(UTC)));
    }

    @Override
    public UserUpdatedEvent validateAndUpdateProfile(User user, String firstName, String lastName, String imageUrl) {
        return null;
    }
}