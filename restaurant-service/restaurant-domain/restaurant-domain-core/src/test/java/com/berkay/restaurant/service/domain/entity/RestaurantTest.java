package com.berkay.restaurant.service.domain.entity;

import com.berkay.domain.valueobject.Money;
import com.berkay.domain.valueobject.ProductCategoryId;
import com.berkay.restaurant.service.domain.exception.RestaurantDomainException;
import com.berkay.restaurant.service.domain.valueobject.RestaurantName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class RestaurantTest {

    private Restaurant restaurant;
    private ProductCategory category1;
    private ProductCategory category2;

    @BeforeEach
    public void setUp() {
        restaurant = Restaurant.builder()
                .restaurantName(new RestaurantName("Test Restaurant"))
                .active(true)
                .categoryVersion(0L)
                .categories(new ArrayList<>())
                .menu(new ArrayList<>())
                .build();

        category1 = ProductCategory.builder()
                .productCategoryId(new ProductCategoryId(UUID.randomUUID()))
                .name("Category 1")
                .sortOrder(1)
                .build();

        category2 = ProductCategory.builder()
                .productCategoryId(new ProductCategoryId(UUID.randomUUID()))
                .name("Category 2")
                .sortOrder(2)
                .build();
    }

    @Test
    public void testUpdateCategoriesSuccess() {
        List<ProductCategory> newCategories = List.of(category1, category2);
        restaurant.updateCategories(newCategories, 0L);

        assertEquals(2, restaurant.getCategories().size());
        assertEquals(1L, restaurant.getCategoryVersion());
    }

    @Test
    public void testUpdateCategoriesOptimisticLockingFailure() {
        List<ProductCategory> newCategories = List.of(category1, category2);
        
        RestaurantDomainException exception = assertThrows(RestaurantDomainException.class, () -> {
            restaurant.updateCategories(newCategories, 1L); // Expected version is 0
        });

        assertEquals("Category version mismatch! Expected: 0, Actual: 1", exception.getMessage());
    }



    @Test
    public void testDeleteCategoryWithProducts() {
        restaurant.updateCategories(List.of(category1, category2), 0L);

        Product product = Product.builder()
                .name("Test Product")
                .price(new Money(new BigDecimal("10.00")))
                .stock(10)
                .available(true)
                .hidden(false)
                .categoryId(category1.getId())
                .build();

        restaurant.addProduct(product);

        List<ProductCategory> newCategories = List.of(category2); // category1 is removed

        RestaurantDomainException exception = assertThrows(RestaurantDomainException.class, () -> {
            restaurant.updateCategories(newCategories, 1L);
        });

        assertEquals("Cannot delete category with id: " + category1.getId().getValue() + " because it contains products.", exception.getMessage());
    }

    @Test
    public void testAddProductWithInvalidCategory() {
        Product product = Product.builder()
                .name("Test Product")
                .price(new Money(new BigDecimal("10.00")))
                .stock(10)
                .available(true)
                .hidden(false)
                .categoryId(new ProductCategoryId(UUID.randomUUID())) // Non-existent category
                .build();

        RestaurantDomainException exception = assertThrows(RestaurantDomainException.class, () -> {
            restaurant.addProduct(product);
        });

        assertEquals("Product category is invalid or does not belong to this restaurant!", exception.getMessage());
    }
}
