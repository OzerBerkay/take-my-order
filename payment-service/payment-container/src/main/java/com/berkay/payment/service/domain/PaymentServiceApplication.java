package com.berkay.payment.service.domain;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EnableJpaRepositories(basePackages = { "com.berkay.payment.service.dataaccess", "com.berkay.dataaccess"})
@EntityScan(basePackages = { "com.berkay.payment.service.dataaccess", "com.berkay.dataaccess"})
@SpringBootApplication(scanBasePackages = "com.berkay")
public class PaymentServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(PaymentServiceApplication.class, args);
    }
}