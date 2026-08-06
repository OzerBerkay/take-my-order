package com.berkay.identity.service.application.initializer;

import com.berkay.identity.service.application.config.SampleUserProperties;
import com.berkay.identity.service.dto.command.RegisterCustomerCommand;
import com.berkay.identity.service.dto.command.RegisterMerchantCommand;
import com.berkay.identity.service.handler.RegisterCustomerCommandHandler;
import com.berkay.identity.service.handler.RegisterMerchantCommandHandler;
import com.berkay.identity.service.ports.output.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@Order(2)
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(prefix = "identity-service.init", name = "create-samples", havingValue = "true")
public class SampleUserInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RegisterCustomerCommandHandler registerCustomerCommandHandler;
    private final RegisterMerchantCommandHandler registerMerchantCommandHandler;
    private final SampleUserProperties properties;

    @Override
    public void run(String... args) {
        log.info("Checking if sample users exist...");
        createSampleCustomer();
        createSampleMerchant();
    }

    private void createSampleCustomer() {
        String email = properties.getCustomer().getEmail();
        if (userRepository.findByEmail(email).isPresent()) {
            log.info("Sample Customer already exists with email: {}", email);
            return;
        }

        log.info("Creating default Sample Customer...");
        RegisterCustomerCommand command = RegisterCustomerCommand.builder()
                .email(email)
                .password(properties.getCustomer().getPassword())
                .firstName(properties.getCustomer().getFirstName())
                .lastName(properties.getCustomer().getLastName())
                .phoneNumber(properties.getCustomer().getPhoneNumber())
                .build();

        try {
            registerCustomerCommandHandler.registerCustomer(command);
            log.info("Sample Customer created successfully.");
        } catch (Exception e) {
            log.error("Failed to create Sample Customer", e);
        }
    }

    private void createSampleMerchant() {
        String email = properties.getMerchant().getEmail();
        if (userRepository.findByEmail(email).isPresent()) {
            log.info("Sample Merchant already exists with email: {}", email);
            return;
        }

        log.info("Creating default Sample Merchant...");
        RegisterMerchantCommand command = RegisterMerchantCommand.builder()
                .email(email)
                .password(properties.getMerchant().getPassword())
                .firstName(properties.getMerchant().getFirstName())
                .lastName(properties.getMerchant().getLastName())
                .phoneNumber(properties.getMerchant().getPhoneNumber())
                .build();

        try {
            registerMerchantCommandHandler.registerMerchant(command);
            log.info("Sample Merchant created successfully.");
        } catch (Exception e) {
            log.error("Failed to create Sample Merchant", e);
        }
    }
}
