package com.berkay.identity.service.dataaccess.user.repository;

import com.berkay.identity.service.dataaccess.user.entity.UserUpdateIntentEntity;
import com.berkay.identity.service.domain.valueobject.IntentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserUpdateIntentJpaRepository extends JpaRepository<UserUpdateIntentEntity, UUID> {
    List<UserUpdateIntentEntity> findByStatusIn(List<IntentStatus> statuses);
}
