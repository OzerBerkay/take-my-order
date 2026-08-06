package com.berkay.restaurant.service.dataaccess;

import org.junit.jupiter.api.Test;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertTrue;

class CacheConfigTest {

    @Test
    void shouldConfigureRequiredCaches() {
        // Arrange
        CacheConfig cacheConfig = new CacheConfig();
        
        // Act
        CacheManager cacheManager = cacheConfig.cacheManager();
        
        // Assert
        assertTrue(cacheManager instanceof CaffeineCacheManager);
        Collection<String> cacheNames = cacheManager.getCacheNames();
        assertTrue(cacheNames.contains("roles"), "CacheManager must contain 'roles' cache");
        assertTrue(cacheNames.contains("roleOrgUnits"), "CacheManager must contain 'roleOrgUnits' cache");
    }
}
