package com.berkay.restaurant.service.domain.ports.input.service.cuisine;

import com.berkay.restaurant.service.domain.dto.create.cuisine.CreateCuisineCommand;
import com.berkay.restaurant.service.domain.dto.create.cuisine.CreateCuisineResponse;
import com.berkay.restaurant.service.domain.dto.delete.cuisine.DeleteCuisineResponse;
import com.berkay.restaurant.service.domain.dto.read.CuisineModel;
import com.berkay.restaurant.service.domain.dto.update.cuisine.UpdateCuisineCommand;
import com.berkay.restaurant.service.domain.dto.update.cuisine.UpdateCuisineResponse;
import com.berkay.restaurant.service.domain.entity.Cuisine;
import com.berkay.restaurant.service.domain.exception.RestaurantDomainException;
import com.berkay.restaurant.service.domain.ports.output.repository.cuisine.CuisineRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CuisineApplicationServiceImpl implements CuisineApplicationService {

    private final CuisineRepository cuisineRepository;

    public CuisineApplicationServiceImpl(CuisineRepository cuisineRepository) {
        this.cuisineRepository = cuisineRepository;
    }

    @Override
    @Transactional
    public CreateCuisineResponse createCuisine(CreateCuisineCommand createCuisineCommand) {
        Optional<Cuisine> existingCuisine = cuisineRepository.findByCode(createCuisineCommand.getCode());
        if (existingCuisine.isPresent()) {
            throw new RestaurantDomainException("Cuisine with code " + createCuisineCommand.getCode() + " already exists!");
        }

        Cuisine cuisine = Cuisine.builder()
                .name(createCuisineCommand.getName())
                .code(createCuisineCommand.getCode())
                .description(createCuisineCommand.getDescription())
                .iconUrl(createCuisineCommand.getIconUrl())
                .active(createCuisineCommand.getIsActive())
                .build();

        cuisine.initializeCuisine();

        Cuisine savedCuisine = cuisineRepository.save(cuisine);
        log.info("Cuisine created with id: {}", savedCuisine.getId().getValue());
        
        return new CreateCuisineResponse(savedCuisine.getId().getValue(), "Cuisine created successfully");
    }

    @Override
    @Transactional
    public UpdateCuisineResponse updateCuisine(UUID cuisineId, UpdateCuisineCommand updateCuisineCommand) {
        Optional<Cuisine> cuisineOpt = cuisineRepository.findById(cuisineId);
        if (cuisineOpt.isEmpty()) {
            throw new RestaurantDomainException("Cuisine with id " + cuisineId + " not found!");
        }

        Cuisine cuisine = cuisineOpt.get();

        String name = updateCuisineCommand.getName() != null ? updateCuisineCommand.getName() : cuisine.getName();
        String code = updateCuisineCommand.getCode() != null ? updateCuisineCommand.getCode() : cuisine.getCode();
        String description = updateCuisineCommand.getDescription() != null ? updateCuisineCommand.getDescription() : cuisine.getDescription();
        String iconUrl = updateCuisineCommand.getIconUrl() != null ? updateCuisineCommand.getIconUrl() : cuisine.getIconUrl();
        boolean active = updateCuisineCommand.getIsActive() != null ? updateCuisineCommand.getIsActive() : cuisine.isActive();

        if (!cuisine.getCode().equals(code)) {
            Optional<Cuisine> existingCuisine = cuisineRepository.findByCode(code);
            if (existingCuisine.isPresent()) {
                throw new RestaurantDomainException("Cuisine with code " + code + " already exists!");
            }
        }

        cuisine.update(name, code, description, iconUrl, active);
        Cuisine savedCuisine = cuisineRepository.save(cuisine);
        log.info("Cuisine updated with id: {}", savedCuisine.getId().getValue());

        return new UpdateCuisineResponse(savedCuisine.getId().getValue(), "Cuisine updated successfully");
    }

    @Override
    @Transactional
    public DeleteCuisineResponse deleteCuisine(UUID cuisineId) {
        Optional<Cuisine> cuisineOpt = cuisineRepository.findById(cuisineId);
        if (cuisineOpt.isEmpty()) {
            throw new RestaurantDomainException("Cuisine with id " + cuisineId + " not found!");
        }

        cuisineRepository.delete(cuisineId);
        log.info("Cuisine deleted with id: {}", cuisineId);
        
        return new DeleteCuisineResponse(cuisineId, "Cuisine deleted successfully");
    }

    @Override
    @Transactional(readOnly = true)
    public List<CuisineModel> getCuisines(Boolean active) {
        List<Cuisine> cuisines;
        if (active == null) {
            cuisines = cuisineRepository.findAll();
        } else {
            cuisines = cuisineRepository.findByIsActive(active);
        }

        return cuisines.stream().map(cuisine -> CuisineModel.builder()
                .id(cuisine.getId().getValue())
                .name(cuisine.getName())
                .code(cuisine.getCode())
                .description(cuisine.getDescription())
                .iconUrl(cuisine.getIconUrl())
                .isActive(cuisine.isActive())
                .build()).collect(Collectors.toList());
    }
}
