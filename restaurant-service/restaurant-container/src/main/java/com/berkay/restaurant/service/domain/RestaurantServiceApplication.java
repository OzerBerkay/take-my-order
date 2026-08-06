package com.berkay.restaurant.service.domain;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EnableJpaRepositories(basePackages = { "com.berkay.restaurant.service.dataaccess"})
@EntityScan(basePackages = { "com.berkay.restaurant.service.dataaccess" })
@SpringBootApplication(scanBasePackages = "com.berkay")
public class RestaurantServiceApplication {
    public static void main(String[] args) {
        System.setProperty("spring.devtools.restart.enabled", "false");
        SpringApplication.run(RestaurantServiceApplication.class, args);
    }
}
