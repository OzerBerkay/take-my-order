package com.berkay.identity.service.dataaccess.user.repository;

import com.berkay.identity.service.dataaccess.user.entity.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserJpaRepository extends JpaRepository<UserEntity, UUID>, JpaSpecificationExecutor<UserEntity> {

    @EntityGraph(attributePaths = {"organizationalUnitIds"})
    Page<UserEntity> findAll(@Nullable Specification<UserEntity> spec, Pageable pageable);

    // Login olurken veya kayıt sırasında email kontrolü için
    Optional<UserEntity> findByEmail(String email);

    // Telefon numarası unique olduğu için kayıt öncesi kontrol amaçlı
    Optional<UserEntity> findByPhoneNumber(String phoneNumber);

    Optional<UserEntity> findByExternalId(String externalId);
}