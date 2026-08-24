package com.berkay.restaurant.service.domain.ports.input.service.cuisine;

import com.berkay.restaurant.service.domain.dto.create.cuisine.CreateCuisineCommand;
import com.berkay.restaurant.service.domain.dto.create.cuisine.CreateCuisineResponse;
import com.berkay.restaurant.service.domain.dto.delete.cuisine.DeleteCuisineResponse;
import com.berkay.restaurant.service.domain.dto.read.CuisineModel;
import com.berkay.restaurant.service.domain.dto.update.cuisine.UpdateCuisineCommand;
import com.berkay.restaurant.service.domain.dto.update.cuisine.UpdateCuisineResponse;

import java.util.List;
import java.util.UUID;

public interface CuisineApplicationService {
    CreateCuisineResponse createCuisine(CreateCuisineCommand createCuisineCommand);
    UpdateCuisineResponse updateCuisine(UUID cuisineId, UpdateCuisineCommand updateCuisineCommand);
    DeleteCuisineResponse deleteCuisine(UUID cuisineId);
    List<CuisineModel> getCuisines(Boolean active);
}
