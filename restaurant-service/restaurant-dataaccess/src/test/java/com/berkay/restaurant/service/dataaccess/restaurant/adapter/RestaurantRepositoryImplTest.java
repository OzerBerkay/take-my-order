package com.berkay.restaurant.service.dataaccess.restaurant.adapter;

import com.berkay.domain.valueobject.RestaurantId;
import com.berkay.restaurant.service.dataaccess.restaurant.entity.ProductEntity;
import com.berkay.restaurant.service.dataaccess.restaurant.entity.RestaurantEntity;
import com.berkay.restaurant.service.dataaccess.restaurant.mapper.RestaurantDataAccessMapper;
import com.berkay.restaurant.service.dataaccess.restaurant.repository.RestaurantJpaRepository;
import com.berkay.restaurant.service.domain.entity.Restaurant;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RestaurantRepositoryImplTest {

    @Mock
    private RestaurantJpaRepository restaurantJpaRepository;

    @Mock
    private RestaurantDataAccessMapper restaurantDataAccessMapper;

    @Mock
    private EntityManager entityManager;

    @InjectMocks
    private RestaurantRepositoryImpl restaurantRepositoryImpl;

    private UUID restaurantId;
    private UUID productId;

    @BeforeEach
    void setUp() {
        restaurantId = UUID.randomUUID();
        productId = UUID.randomUUID();
        ReflectionTestUtils.setField(restaurantRepositoryImpl, "entityManager", entityManager);
    }

    @Test
    void shouldFindRestaurantById() {
        RestaurantEntity entity = RestaurantEntity.builder().restaurantId(restaurantId).build();
        Restaurant domain = mock(Restaurant.class);

        when(restaurantJpaRepository.findById(restaurantId)).thenReturn(Optional.of(entity));
        when(restaurantDataAccessMapper.restaurantEntityToRestaurant(entity)).thenReturn(domain);

        Optional<Restaurant> result = restaurantRepositoryImpl.findRestaurantById(restaurantId);

        assertTrue(result.isPresent());
        assertEquals(domain, result.get());
        verify(restaurantJpaRepository, times(1)).findById(restaurantId);
    }

    @Test
    void shouldFindRestaurantByIdWithLock() {
        RestaurantEntity entity = RestaurantEntity.builder().restaurantId(restaurantId).build();
        Restaurant domain = mock(Restaurant.class);

        when(restaurantJpaRepository.findByIdWithLock(restaurantId)).thenReturn(Optional.of(entity));
        when(restaurantDataAccessMapper.restaurantEntityToRestaurant(entity)).thenReturn(domain);

        Optional<Restaurant> result = restaurantRepositoryImpl.findRestaurantByIdWithLock(restaurantId);

        assertTrue(result.isPresent());
        assertEquals(domain, result.get());
        verify(restaurantJpaRepository, times(1)).findByIdWithLock(restaurantId);
    }

    @Test
    void shouldPreserveProductVersionsWhenSavingRestaurant() {
        Restaurant domainRestaurant = mock(Restaurant.class);
        when(domainRestaurant.getId()).thenReturn(new RestaurantId(restaurantId));

        ProductEntity mappedProduct = ProductEntity.builder()
                .productId(productId)
                .name("Test Burger")
                .version(null)
                .build();

        RestaurantEntity mappedEntity = RestaurantEntity.builder()
                .restaurantId(restaurantId)
                .menu(new ArrayList<>(List.of(mappedProduct)))
                .build();

        ProductEntity existingProduct = ProductEntity.builder()
                .productId(productId)
                .name("Test Burger")
                .version(3)
                .build();

        RestaurantEntity existingEntity = RestaurantEntity.builder()
                .restaurantId(restaurantId)
                .version(1)
                .menu(new ArrayList<>(List.of(existingProduct)))
                .build();

        when(restaurantDataAccessMapper.restaurantToRestaurantEntity(domainRestaurant)).thenReturn(mappedEntity);
        when(restaurantJpaRepository.findById(restaurantId)).thenReturn(Optional.of(existingEntity));
        when(entityManager.merge(mappedEntity)).thenReturn(mappedEntity);
        when(restaurantDataAccessMapper.restaurantEntityToRestaurant(mappedEntity)).thenReturn(domainRestaurant);

        Restaurant result = restaurantRepositoryImpl.saveRestaurant(domainRestaurant);

        assertNotNull(result);
        assertEquals(1, mappedEntity.getVersion());
        assertEquals(3, mappedProduct.getVersion(), "Product version should be preserved from existing managed entity");
        verify(entityManager, times(1)).detach(existingEntity);
        verify(entityManager, times(1)).merge(mappedEntity);
    }
}
