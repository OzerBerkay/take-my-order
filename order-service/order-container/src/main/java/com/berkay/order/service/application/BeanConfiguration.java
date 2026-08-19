package com.berkay.order.service.application;

import com.berkay.order.service.domain.OrderDomainService;
import com.berkay.order.service.domain.OrderDomainServiceImpl;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanConfiguration {

    @Bean
    public OrderDomainService orderDomainService() {
        return new OrderDomainServiceImpl();
    }

    @Bean
    @org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(prefix = "order-service.init", name = "reset-db", havingValue = "true")
    public FlywayMigrationStrategy cleanMigrateStrategy() {
        return flyway -> {
            flyway.clean();
            flyway.migrate();
        };
    }
}