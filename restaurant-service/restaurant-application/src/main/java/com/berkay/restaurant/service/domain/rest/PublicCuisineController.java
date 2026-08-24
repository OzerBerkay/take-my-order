package com.berkay.restaurant.service.domain.rest;

import com.berkay.restaurant.service.domain.dto.read.CuisineModel;
import com.berkay.restaurant.service.domain.ports.input.service.cuisine.CuisineApplicationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping(value = "/public/cuisines", produces = "application/vnd.api.v1+json")
public class PublicCuisineController {

    private final CuisineApplicationService cuisineApplicationService;

    public PublicCuisineController(CuisineApplicationService cuisineApplicationService) {
        this.cuisineApplicationService = cuisineApplicationService;
    }

    @GetMapping
    public ResponseEntity<List<CuisineModel>> getPublicCuisines() {
        log.info("Getting all active public cuisines");
        // Only return active cuisines for public endpoint
        List<CuisineModel> response = cuisineApplicationService.getCuisines(true);
        return ResponseEntity.ok(response);
    }
}
