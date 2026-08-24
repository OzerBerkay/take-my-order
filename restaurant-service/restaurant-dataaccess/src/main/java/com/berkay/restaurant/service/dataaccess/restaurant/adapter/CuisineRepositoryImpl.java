package com.berkay.restaurant.service.dataaccess.restaurant.adapter;

import com.berkay.restaurant.service.dataaccess.restaurant.entity.CuisineEntity;
import com.berkay.restaurant.service.dataaccess.restaurant.mapper.CuisineDataAccessMapper;
import com.berkay.restaurant.service.dataaccess.restaurant.repository.CuisineJpaRepository;
import com.berkay.restaurant.service.domain.entity.Cuisine;
import com.berkay.restaurant.service.domain.ports.output.repository.cuisine.CuisineRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class CuisineRepositoryImpl implements CuisineRepository {

    private final CuisineJpaRepository cuisineJpaRepository;
    private final CuisineDataAccessMapper cuisineDataAccessMapper;

    public CuisineRepositoryImpl(CuisineJpaRepository cuisineJpaRepository, CuisineDataAccessMapper cuisineDataAccessMapper) {
        this.cuisineJpaRepository = cuisineJpaRepository;
        this.cuisineDataAccessMapper = cuisineDataAccessMapper;
    }

    @Override
    public Cuisine save(Cuisine cuisine) {
        CuisineEntity cuisineEntity = cuisineDataAccessMapper.cuisineToCuisineEntity(cuisine);
        CuisineEntity savedEntity = cuisineJpaRepository.save(cuisineEntity);
        return cuisineDataAccessMapper.cuisineEntityToCuisine(savedEntity);
    }

    @Override
    public Optional<Cuisine> findById(UUID cuisineId) {
        return cuisineJpaRepository.findById(cuisineId)
                .map(cuisineDataAccessMapper::cuisineEntityToCuisine);
    }

    @Override
    public Optional<Cuisine> findByCode(String code) {
        return cuisineJpaRepository.findByCode(code)
                .map(cuisineDataAccessMapper::cuisineEntityToCuisine);
    }

    @Override
    public void delete(UUID cuisineId) {
        cuisineJpaRepository.deleteById(cuisineId);
    }

    @Override
    public List<Cuisine> findAll() {
        return cuisineJpaRepository.findAll().stream()
                .map(cuisineDataAccessMapper::cuisineEntityToCuisine)
                .collect(Collectors.toList());
    }

    @Override
    public List<Cuisine> findByIsActive(Boolean isActive) {
        return cuisineJpaRepository.findByIsActive(isActive).stream()
                .map(cuisineDataAccessMapper::cuisineEntityToCuisine)
                .collect(Collectors.toList());
    }
}
