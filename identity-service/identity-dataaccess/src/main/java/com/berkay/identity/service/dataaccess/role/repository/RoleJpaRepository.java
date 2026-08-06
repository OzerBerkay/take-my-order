package com.berkay.identity.service.dataaccess.role.repository;

import com.berkay.identity.service.dataaccess.role.entity.RoleEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RoleJpaRepository extends JpaRepository<RoleEntity, UUID>, JpaSpecificationExecutor<RoleEntity> {

    Page<RoleEntity> findAll(@Nullable Specification<RoleEntity> spec, Pageable pageable);

    // Yeni rol oluştururken isim ve context çakışmasını kontrol etmek için
    boolean existsByNameAndOrganizationalUnitId(String name, UUID organizationalUnitId);

    // Güncelleme yaparken, kendisi hariç aynı isimde başka rol var mı kontrolü
    boolean existsByNameAndOrganizationalUnitIdAndIdNot(String name, UUID organizationalUnitId, UUID id);

    Optional<RoleEntity> findByNameAndOrganizationalUnitId(String name, UUID organizationalUnitId);

    Optional<RoleEntity> findByName(String name);

    List<RoleEntity> findByUpdatedAtGreaterThanOrderByUpdatedAtAsc(ZonedDateTime updatedAt, Pageable pageable);
}