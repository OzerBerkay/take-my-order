package com.berkay.restaurant.service.domain.ports.output.repository.cuisine;

import com.berkay.restaurant.service.domain.entity.Cuisine;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CuisineRepository {
    Cuisine save(Cuisine cuisine);
    Optional<Cuisine> findById(UUID cuisineId);
    Optional<Cuisine> findByCode(String code);
    void delete(UUID cuisineId);
    List<Cuisine> findAll();
    List<Cuisine> findByIsActive(Boolean isActive);
}
