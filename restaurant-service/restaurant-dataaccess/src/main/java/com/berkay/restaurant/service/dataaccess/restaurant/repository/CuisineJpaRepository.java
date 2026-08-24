package com.berkay.restaurant.service.dataaccess.restaurant.repository;

import com.berkay.restaurant.service.dataaccess.restaurant.entity.CuisineEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;
import java.util.List;

@Repository
public interface CuisineJpaRepository extends JpaRepository<CuisineEntity, UUID> {
    Optional<CuisineEntity> findByCode(String code);
    List<CuisineEntity> findByIsActive(Boolean isActive);
}
