package com.berkay.identity.service.ports.output.repository;

import com.berkay.identity.service.domain.entity.UserUpdateIntent;
import com.berkay.identity.service.domain.valueobject.IntentStatus;

import java.util.List;
import java.util.Optional;

public interface UserUpdateIntentRepository {
    UserUpdateIntent save(UserUpdateIntent intent);
    Optional<UserUpdateIntent> findById(String intentId);
    List<UserUpdateIntent> findByStatusIn(List<IntentStatus> statuses);
}
