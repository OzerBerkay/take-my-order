package com.berkay.customer.service.config;

import com.berkay.customer.service.domain.CustomerDomainService;
import com.berkay.customer.service.domain.CustomerDomainServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanConfiguration {

    @Bean
    public CustomerDomainService customerDomainService() {
        return new CustomerDomainServiceImpl();
    }
}
