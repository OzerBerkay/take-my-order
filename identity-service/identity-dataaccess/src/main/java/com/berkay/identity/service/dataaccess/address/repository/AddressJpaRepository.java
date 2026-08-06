package com.berkay.identity.service.dataaccess.address.repository;

import com.berkay.identity.service.dataaccess.address.entity.AddressEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

import org.springframework.stereotype.Repository;

public interface AddressJpaRepository extends JpaRepository<AddressEntity, UUID> {
    java.util.List<AddressEntity> findByUserId(UUID userId);
}
