package com.berkay.identity.service.application.security.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
public class CacheConfig {
    // YAML konfigürasyonu ve Spring'in default CacheManager'ı bizim için Caffeine'i otomatik ayağa kaldıracak.
}