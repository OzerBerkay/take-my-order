package com.berkay.identity.service.domain;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

// Component Scan "com.berkay" seviyesinden başlar ki altyapıdaki (infrastructure) Kafka producer bean'leri bulunabilsin.
@EnableScheduling
@SpringBootApplication(scanBasePackages = {"com.berkay.identity", "com.berkay.kafka", "com.berkay.application.handler"})
@EnableJpaRepositories(basePackages = "com.berkay.identity.service.dataaccess")
@EntityScan(basePackages = "com.berkay.identity.service.dataaccess")
public class IdentityServiceApplication {
    public static void main(String[] args) {
        System.setProperty("spring.devtools.restart.enabled", "false");
        SpringApplication.run(IdentityServiceApplication.class, args);
    }
}