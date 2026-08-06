package com.berkay.identity.service.dataaccess.user.mapper;

import com.berkay.identity.service.dataaccess.user.entity.UserUpdateIntentEntity;
import com.berkay.identity.service.domain.entity.UserUpdateIntent;
import com.berkay.identity.service.domain.valueobject.IntentId;
import com.berkay.identity.service.domain.valueobject.UserId;
import org.springframework.stereotype.Component;

@Component
public class UserUpdateIntentDataAccessMapper {

    public UserUpdateIntentEntity userUpdateIntentToUserUpdateIntentEntity(UserUpdateIntent intent) {
        return UserUpdateIntentEntity.builder()
                .id(intent.getId().getValue())
                .userId(intent.getUserId().getValue())
                .status(intent.getStatus())
                .commandType(intent.getCommandType())
                .oldSnapshot(intent.getOldSnapshot())
                .newSnapshot(intent.getNewSnapshot())
                .createdAt(intent.getCreatedAt())
                .lockedAt(intent.getLockedAt())
                .lockedBy(intent.getLockedBy())
                .build();
    }

    public UserUpdateIntent userUpdateIntentEntityToUserUpdateIntent(UserUpdateIntentEntity entity) {
        return UserUpdateIntent.builder()
                .intentId(new IntentId(entity.getId()))
                .userId(new UserId(entity.getUserId()))
                .status(entity.getStatus())
                .commandType(entity.getCommandType())
                .oldSnapshot(entity.getOldSnapshot())
                .newSnapshot(entity.getNewSnapshot())
                .createdAt(entity.getCreatedAt())
                .lockedAt(entity.getLockedAt())
                .lockedBy(entity.getLockedBy())
                .build();
    }
}
