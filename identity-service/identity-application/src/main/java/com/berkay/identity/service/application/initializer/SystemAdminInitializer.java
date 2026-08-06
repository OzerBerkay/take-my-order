package com.berkay.identity.service.application.initializer;

import com.berkay.identity.service.domain.constants.RoleConstants;
import com.berkay.identity.service.domain.entity.Role;
import com.berkay.identity.service.dto.command.RegisterInternalUserCommand;
import com.berkay.identity.service.handler.RegisterInternalUserCommandHandler;
import com.berkay.identity.service.ports.output.repository.UserRepository;
import com.berkay.identity.service.application.config.SystemAdminProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class SystemAdminInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RegisterInternalUserCommandHandler registerInternalUserCommandHandler;
    private final SystemAdminProperties properties;

    /**
     * @PostConstruct yerine ApplicationReadyEvent kullanılmıştır.
     * Nedeni: Veritabanı migrasyonlarının (Flyway) tamamen bitmesi, 
     * Transaction yönetiminin (AOP) hazır olması ve olası bir Keycloak bağlantı 
     * hatasında uygulamanın çökmesini (Fatal Error) engellemek amacıyla 
     * uygulamanın tam anlamıyla hazır olması beklenmektedir.
     */
    @Override
    @Order(2)
    public void run(String... args) {
        log.info("Checking if system admin exists...");

        if (userRepository.findByEmail(properties.getEmail()).isPresent()) {
            log.info("System admin already exists with email: {}", properties.getEmail());
            return;
        }

        log.info("System admin not found. Creating default system admin...");

        Role adminRole = userRepository.findRoleByName(RoleConstants.SYSTEM_ADMIN)
                .orElseThrow(() -> new IllegalStateException("SYSTEM_ADMIN role not found in database! Please ensure Flyway migrations ran correctly."));

        RegisterInternalUserCommand command = RegisterInternalUserCommand.builder()
                .email(properties.getEmail())
                .password(properties.getPassword())
                .firstName(properties.getFirstName())
                .lastName(properties.getLastName())
                .phoneNumber(properties.getPhoneNumber())
                .roleIds(List.of(adminRole.getId().getValue()))
                .build();

        try {
            registerInternalUserCommandHandler.registerInternalUser(command);
            log.info("Default system admin created successfully.");
        } catch (Exception e) {
            log.error("Failed to create default system admin", e);
        }
    }
}
