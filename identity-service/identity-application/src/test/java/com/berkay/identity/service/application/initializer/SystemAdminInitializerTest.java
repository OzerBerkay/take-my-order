package com.berkay.identity.service.application.initializer;

import com.berkay.identity.service.application.config.SystemAdminProperties;
import com.berkay.identity.service.domain.constants.RoleConstants;
import com.berkay.identity.service.domain.entity.Role;
import com.berkay.identity.service.domain.entity.User;
import com.berkay.identity.service.domain.valueobject.RoleId;
import com.berkay.identity.service.dto.command.RegisterInternalUserCommand;
import com.berkay.identity.service.dto.command.CreateUserResponse;
import com.berkay.identity.service.handler.RegisterInternalUserCommandHandler;
import com.berkay.identity.service.ports.output.repository.RoleRepository;
import com.berkay.identity.service.ports.output.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.context.event.ApplicationReadyEvent;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SystemAdminInitializerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RegisterInternalUserCommandHandler registerInternalUserCommandHandler;

    @Mock
    private SystemAdminProperties properties;

    @InjectMocks
    private SystemAdminInitializer initializer;

    @BeforeEach
    void setUp() {
        // Testler öncesi varsayılan özellikleri dönecek şekilde mocklayalım
        when(properties.getEmail()).thenReturn("admin@takemyorder.com");
    }

    @Test
    @DisplayName("Başarılı Senaryo: Veritabanında admin yoksa oluşturulmalıdır.")
    void shouldCreateAdmin_WhenNotExists() {
        when(userRepository.findByEmail("admin@takemyorder.com")).thenReturn(Optional.empty());

        Role adminRole = mock(Role.class);
        RoleId roleId = new RoleId(UUID.randomUUID());
        when(adminRole.getId()).thenReturn(roleId);
        
        when(userRepository.findRoleByName(RoleConstants.SYSTEM_ADMIN)).thenReturn(Optional.of(adminRole));
        when(properties.getPassword()).thenReturn("Admin123!");
        when(properties.getFirstName()).thenReturn("System");
        when(properties.getLastName()).thenReturn("Admin");
        when(properties.getPhoneNumber()).thenReturn("+905550000000");

        initializer.run();

        verify(registerInternalUserCommandHandler).registerInternalUser(any(RegisterInternalUserCommand.class));
    }

    @Test
    @DisplayName("Alternatif Senaryo: Veritabanında admin zaten varsa işlem atlanmalıdır (Skip).")
    void shouldSkipCreation_WhenAdminAlreadyExists() {
        when(userRepository.findByEmail("admin@takemyorder.com")).thenReturn(Optional.of(mock(User.class)));

        initializer.run();

        verify(userRepository, never()).findRoleByName(any());
        verify(registerInternalUserCommandHandler, never()).registerInternalUser(any());
    }

    @Test
    @DisplayName("Hata Senaryosu: SYSTEM_ADMIN rolü veritabanında bulunamazsa Exception fırlatmalıdır.")
    void shouldThrowException_WhenRoleNotFound() {
        when(userRepository.findByEmail("admin@takemyorder.com")).thenReturn(Optional.empty());
        when(userRepository.findRoleByName(RoleConstants.SYSTEM_ADMIN)).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class, () -> initializer.run());

        verify(registerInternalUserCommandHandler, never()).registerInternalUser(any());
    }
}
