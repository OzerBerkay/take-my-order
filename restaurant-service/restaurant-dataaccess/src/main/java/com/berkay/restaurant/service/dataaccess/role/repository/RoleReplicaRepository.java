package com.berkay.restaurant.service.dataaccess.role.repository;

import com.berkay.restaurant.service.dataaccess.role.entity.RoleReplicaEntity;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RoleReplicaRepository extends JpaRepository<RoleReplicaEntity, UUID> {

    Optional<RoleReplicaEntity> findById(UUID id);

    @Override
    @CacheEvict(value = "roles", key = "#entity.id")
    <S extends RoleReplicaEntity> S save(S entity);

    @Override
    @CacheEvict(value = "roles", key = "#id")
    void deleteById(UUID id);
}
