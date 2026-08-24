package com.berkay.restaurant.service.domain;

import com.berkay.restaurant.service.domain.dto.create.cuisine.CreateCuisineCommand;
import com.berkay.restaurant.service.domain.dto.create.cuisine.CreateCuisineResponse;
import com.berkay.restaurant.service.domain.dto.delete.cuisine.DeleteCuisineResponse;
import com.berkay.restaurant.service.domain.dto.read.CuisineModel;
import com.berkay.restaurant.service.domain.dto.update.cuisine.UpdateCuisineCommand;
import com.berkay.restaurant.service.domain.dto.update.cuisine.UpdateCuisineResponse;
import com.berkay.restaurant.service.domain.entity.Cuisine;
import com.berkay.restaurant.service.domain.exception.RestaurantDomainException;
import com.berkay.restaurant.service.domain.ports.input.service.cuisine.CuisineApplicationService;
import com.berkay.restaurant.service.domain.ports.input.service.cuisine.CuisineApplicationServiceImpl;
import com.berkay.restaurant.service.domain.ports.output.repository.cuisine.CuisineRepository;
import com.berkay.restaurant.service.domain.valueobject.CuisineId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CuisineApplicationServiceTest {

    @Mock
    private CuisineRepository cuisineRepository;

    @InjectMocks
    private CuisineApplicationServiceImpl cuisineApplicationService;

    private UUID cuisineId;
    private Cuisine cuisine;

    @BeforeEach
    void setUp() {
        cuisineId = UUID.randomUUID();
        cuisine = Cuisine.builder()
                .cuisineId(new CuisineId(cuisineId))
                .name("Turkish")
                .code("turkish")
                .description("Turkish food")
                .iconUrl("http://image.com/turkish.png")
                .active(true)
                .build();
    }

    @Test
    void testCreateCuisine_Success() {
        CreateCuisineCommand command = CreateCuisineCommand.builder()
                .name("Mexican")
                .code("mexican")
                .description("Mexican food")
                .iconUrl("http://image.com/mexican.png")
                .isActive(true)
                .build();

        when(cuisineRepository.findByCode("mexican")).thenReturn(Optional.empty());
        when(cuisineRepository.save(any(Cuisine.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CreateCuisineResponse response = cuisineApplicationService.createCuisine(command);

        assertNotNull(response);
        assertNotNull(response.getCuisineId());
        assertEquals("Cuisine created successfully", response.getMessage());

        verify(cuisineRepository, times(1)).findByCode("mexican");
        verify(cuisineRepository, times(1)).save(any(Cuisine.class));
    }

    @Test
    void testCreateCuisine_AlreadyExists() {
        CreateCuisineCommand command = CreateCuisineCommand.builder()
                .name("Mexican")
                .code("mexican")
                .description("Mexican food")
                .iconUrl("http://image.com/mexican.png")
                .isActive(true)
                .build();

        when(cuisineRepository.findByCode("mexican")).thenReturn(Optional.of(cuisine));

        RestaurantDomainException exception = assertThrows(RestaurantDomainException.class,
                () -> cuisineApplicationService.createCuisine(command));

        assertEquals("Cuisine with code mexican already exists!", exception.getMessage());
        verify(cuisineRepository, never()).save(any(Cuisine.class));
    }

    @Test
    void testCreateCuisine_InvalidCodeFormat() {
        CreateCuisineCommand command = CreateCuisineCommand.builder()
                .name("Mexican")
                .code("Mexican Food")
                .description("Mexican food")
                .iconUrl("http://image.com/mexican.png")
                .isActive(true)
                .build();

        when(cuisineRepository.findByCode("Mexican Food")).thenReturn(Optional.empty());

        RestaurantDomainException exception = assertThrows(RestaurantDomainException.class,
                () -> cuisineApplicationService.createCuisine(command));

        assertEquals("Cuisine code must be lower-case and snake_case format! (e.g. fast_food)", exception.getMessage());
        verify(cuisineRepository, never()).save(any(Cuisine.class));
    }

    @Test
    void testUpdateCuisine_Success() {
        UpdateCuisineCommand command = UpdateCuisineCommand.builder()
                .name("Turkish Updated")
                .code("turkish")
                .isActive(false)
                .build();

        when(cuisineRepository.findById(cuisineId)).thenReturn(Optional.of(cuisine));
        when(cuisineRepository.save(any(Cuisine.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UpdateCuisineResponse response = cuisineApplicationService.updateCuisine(cuisineId, command);

        assertNotNull(response);
        assertEquals(cuisineId, response.getCuisineId());
        assertEquals("Cuisine updated successfully", response.getMessage());

        ArgumentCaptor<Cuisine> cuisineCaptor = ArgumentCaptor.forClass(Cuisine.class);
        verify(cuisineRepository, times(1)).save(cuisineCaptor.capture());

        Cuisine savedCuisine = cuisineCaptor.getValue();
        assertEquals("Turkish Updated", savedCuisine.getName());
        assertEquals("turkish", savedCuisine.getCode());
        assertFalse(savedCuisine.isActive());
        assertEquals("Turkish food", savedCuisine.getDescription()); // unchanged
    }

    @Test
    void testUpdateCuisine_CodeChangeConflict() {
        UpdateCuisineCommand command = UpdateCuisineCommand.builder()
                .code("new_code")
                .build();

        when(cuisineRepository.findById(cuisineId)).thenReturn(Optional.of(cuisine));
        when(cuisineRepository.findByCode("new_code")).thenReturn(Optional.of(Cuisine.builder().build()));

        RestaurantDomainException exception = assertThrows(RestaurantDomainException.class,
                () -> cuisineApplicationService.updateCuisine(cuisineId, command));

        assertEquals("Cuisine with code new_code already exists!", exception.getMessage());
        verify(cuisineRepository, never()).save(any(Cuisine.class));
    }

    @Test
    void testUpdateCuisine_NotFound() {
        UpdateCuisineCommand command = UpdateCuisineCommand.builder().build();

        when(cuisineRepository.findById(cuisineId)).thenReturn(Optional.empty());

        RestaurantDomainException exception = assertThrows(RestaurantDomainException.class,
                () -> cuisineApplicationService.updateCuisine(cuisineId, command));

        assertEquals("Cuisine with id " + cuisineId + " not found!", exception.getMessage());
    }

    @Test
    void testDeleteCuisine_Success() {
        when(cuisineRepository.findById(cuisineId)).thenReturn(Optional.of(cuisine));
        
        DeleteCuisineResponse response = cuisineApplicationService.deleteCuisine(cuisineId);
        
        assertNotNull(response);
        assertEquals(cuisineId, response.getCuisineId());
        assertEquals("Cuisine deleted successfully", response.getMessage());
        
        verify(cuisineRepository, times(1)).delete(cuisineId);
    }
    
    @Test
    void testDeleteCuisine_NotFound() {
        when(cuisineRepository.findById(cuisineId)).thenReturn(Optional.empty());
        
        RestaurantDomainException exception = assertThrows(RestaurantDomainException.class,
                () -> cuisineApplicationService.deleteCuisine(cuisineId));

        assertEquals("Cuisine with id " + cuisineId + " not found!", exception.getMessage());
        verify(cuisineRepository, never()).delete(any());
    }

    @Test
    void testGetCuisines_All() {
        when(cuisineRepository.findAll()).thenReturn(List.of(cuisine));
        
        List<CuisineModel> cuisines = cuisineApplicationService.getCuisines(null);
        
        assertEquals(1, cuisines.size());
        assertEquals("turkish", cuisines.get(0).getCode());
        verify(cuisineRepository, times(1)).findAll();
        verify(cuisineRepository, never()).findByIsActive(anyBoolean());
    }

    @Test
    void testGetCuisines_ActiveOnly() {
        when(cuisineRepository.findByIsActive(true)).thenReturn(List.of(cuisine));
        
        List<CuisineModel> cuisines = cuisineApplicationService.getCuisines(true);
        
        assertEquals(1, cuisines.size());
        assertEquals("turkish", cuisines.get(0).getCode());
        verify(cuisineRepository, never()).findAll();
        verify(cuisineRepository, times(1)).findByIsActive(true);
    }
}
