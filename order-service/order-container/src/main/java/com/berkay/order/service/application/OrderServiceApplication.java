package com.berkay.order.service.application;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EnableJpaRepositories(basePackages = { "com.berkay.order.service.dataaccess"})
@EntityScan(basePackages = { "com.berkay.order.service.dataaccess"})
@SpringBootApplication(scanBasePackages = "com.berkay")
public class OrderServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(OrderServiceApplication.class, args);
    }
}
