package com.berkay.identity.service.dataaccess.user.repository;

import com.berkay.identity.service.dataaccess.user.entity.UserRoleEntity;
import com.berkay.identity.service.dataaccess.user.entity.UserRoleId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserRoleJpaRepository extends JpaRepository<UserRoleEntity, UserRoleId> {
    List<UserRoleEntity> findByUserId(UUID userId);
    List<UserRoleEntity> findByUserIdIn(List<UUID> userIds);
    void deleteByUserId(UUID userId);
}
