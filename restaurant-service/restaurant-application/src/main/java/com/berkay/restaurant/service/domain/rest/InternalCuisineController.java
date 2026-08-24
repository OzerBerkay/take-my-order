package com.berkay.restaurant.service.domain.rest;

import com.berkay.restaurant.service.domain.dto.create.cuisine.CreateCuisineCommand;
import com.berkay.restaurant.service.domain.dto.create.cuisine.CreateCuisineResponse;
import com.berkay.restaurant.service.domain.dto.delete.cuisine.DeleteCuisineResponse;
import com.berkay.restaurant.service.domain.dto.read.CuisineModel;
import com.berkay.restaurant.service.domain.dto.update.cuisine.UpdateCuisineCommand;
import com.berkay.restaurant.service.domain.dto.update.cuisine.UpdateCuisineResponse;
import com.berkay.restaurant.service.domain.ports.input.service.cuisine.CuisineApplicationService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping(value = "/cuisines", produces = "application/vnd.api.v1+json")
public class InternalCuisineController {

    private final CuisineApplicationService cuisineApplicationService;

    public InternalCuisineController(CuisineApplicationService cuisineApplicationService) {
        this.cuisineApplicationService = cuisineApplicationService;
    }

    @PostMapping
    @PreAuthorize("@restaurantAuthService.hasPermission(authentication, 'can_create_cuisine_type')")
    public ResponseEntity<CreateCuisineResponse> createCuisine(@RequestBody @Valid CreateCuisineCommand createCuisineCommand) {
        log.info("Creating new cuisine with name: {}", createCuisineCommand.getName());
        CreateCuisineResponse response = cuisineApplicationService.createCuisine(createCuisineCommand);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{cuisineId}")
    @PreAuthorize("@restaurantAuthService.hasPermission(authentication, 'can_update_cuisine_type')")
    public ResponseEntity<UpdateCuisineResponse> updateCuisine(@PathVariable UUID cuisineId,
                                                               @RequestBody @Valid UpdateCuisineCommand updateCuisineCommand) {
        log.info("Updating cuisine with id: {}", cuisineId);
        UpdateCuisineResponse response = cuisineApplicationService.updateCuisine(cuisineId, updateCuisineCommand);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{cuisineId}")
    @PreAuthorize("@restaurantAuthService.hasPermission(authentication, 'can_delete_cuisine_type')")
    public ResponseEntity<DeleteCuisineResponse> deleteCuisine(@PathVariable UUID cuisineId) {
        log.info("Deleting cuisine with id: {}", cuisineId);
        DeleteCuisineResponse response = cuisineApplicationService.deleteCuisine(cuisineId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @PreAuthorize("@restaurantAuthService.hasPermission(authentication, 'can_read_cuisine_type')")
    public ResponseEntity<List<CuisineModel>> getCuisines(@RequestParam(required = false) Boolean active) {
        log.info("Getting cuisines with active status: {}", active);
        List<CuisineModel> response = cuisineApplicationService.getCuisines(active);
        return ResponseEntity.ok(response);
    }
}
