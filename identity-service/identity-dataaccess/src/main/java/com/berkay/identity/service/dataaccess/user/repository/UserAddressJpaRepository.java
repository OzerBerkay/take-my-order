package com.berkay.identity.service.dataaccess.user.repository;

import com.berkay.identity.service.dataaccess.user.entity.UserAddressEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Repository;

public interface UserAddressJpaRepository extends JpaRepository<UserAddressEntity, UUID> {
    List<UserAddressEntity> findByUserId(UUID userId);
}
