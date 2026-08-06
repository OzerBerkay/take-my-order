package com.berkay.identity.service.dataaccess.user.adapter;

import com.berkay.identity.service.dataaccess.user.mapper.UserUpdateIntentDataAccessMapper;
import com.berkay.identity.service.dataaccess.user.repository.UserUpdateIntentJpaRepository;
import com.berkay.identity.service.domain.entity.UserUpdateIntent;
import com.berkay.identity.service.domain.valueobject.IntentStatus;
import com.berkay.identity.service.ports.output.repository.UserUpdateIntentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class UserUpdateIntentRepositoryImpl implements UserUpdateIntentRepository {

    private final UserUpdateIntentJpaRepository userUpdateIntentJpaRepository;
    private final UserUpdateIntentDataAccessMapper userUpdateIntentDataAccessMapper;

    @Override
    public UserUpdateIntent save(UserUpdateIntent intent) {
        com.berkay.identity.service.dataaccess.user.entity.UserUpdateIntentEntity entity = 
                userUpdateIntentDataAccessMapper.userUpdateIntentToUserUpdateIntentEntity(intent);
                
        // Eger veritabaninda varsa versiyon numarasini alalim, boylece Spring Data JPA persist() yerine merge() cagirir.
        userUpdateIntentJpaRepository.findById(entity.getId()).ifPresent(existing -> {
            entity.setVersion(existing.getVersion());
            entity.setCreatedAt(existing.getCreatedAt());
        });

        return userUpdateIntentDataAccessMapper.userUpdateIntentEntityToUserUpdateIntent(
                userUpdateIntentJpaRepository.save(entity)
        );
    }

    @Override
    public Optional<UserUpdateIntent> findById(String intentId) {
        return userUpdateIntentJpaRepository.findById(UUID.fromString(intentId))
                .map(userUpdateIntentDataAccessMapper::userUpdateIntentEntityToUserUpdateIntent);
    }

    @Override
    public List<UserUpdateIntent> findByStatusIn(List<IntentStatus> statuses) {
        return userUpdateIntentJpaRepository.findByStatusIn(statuses).stream()
                .map(userUpdateIntentDataAccessMapper::userUpdateIntentEntityToUserUpdateIntent)
                .collect(Collectors.toList());
    }
}
