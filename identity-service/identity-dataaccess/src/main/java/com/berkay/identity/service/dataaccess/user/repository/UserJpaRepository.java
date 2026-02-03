package com.berkay.identity.service.dataaccess.user.repository;

import com.berkay.identity.service.dataaccess.user.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserJpaRepository extends JpaRepository<UserEntity, UUID> {

    // Login olurken veya kayıt sırasında email kontrolü için
    Optional<UserEntity> findByEmail(String email);

    // Telefon numarası unique olduğu için kayıt öncesi kontrol amaçlı
    Optional<UserEntity> findByPhoneNumber(String phoneNumber);
}