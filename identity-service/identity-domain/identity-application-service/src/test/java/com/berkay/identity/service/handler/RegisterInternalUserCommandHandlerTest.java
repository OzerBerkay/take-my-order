package com.berkay.identity.service.handler;

import com.berkay.identity.service.domain.IdentityDomainService;
import com.berkay.identity.service.domain.entity.Role;
import com.berkay.identity.service.domain.entity.User;
import com.berkay.identity.service.domain.exception.IdentityDomainException;
import com.berkay.identity.service.domain.valueobject.UserId;
import com.berkay.identity.service.dto.command.CreateUserResponse;
import com.berkay.identity.service.dto.command.RegisterInternalUserCommand;
import com.berkay.identity.service.handler.helper.UserCreateHelper;
import com.berkay.identity.service.mapper.UserDataMapper;
import com.berkay.identity.service.ports.output.repository.IdentityProviderPort;
import com.berkay.identity.service.ports.output.repository.UserRepository;
import com.berkay.identity.service.ports.output.repository.AddressRepository;
import com.berkay.identity.service.dto.command.CreateAddressCommand;
import com.berkay.identity.service.domain.entity.Address;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegisterInternalUserCommandHandlerTest {

    @Mock
    private IdentityDomainService identityDomainService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AddressRepository addressRepository;

    @Mock
    private UserDataMapper userDataMapper;

    @Mock
    private IdentityProviderPort identityProviderPort;

    @Mock
    private UserCreateHelper userCreateHelper;

    @InjectMocks
    private RegisterInternalUserCommandHandler handler;

    @Test
    @DisplayName("Başarılı Senaryo (Best Case): Geçerli rol listesi ve verilerle iç personelin (Internal) başarıyla kaydedilmesi")
    void registerInternalUser_ShouldSucceed_BestCase() {
        // Arrange
        UUID roleId = UUID.randomUUID();
        RegisterInternalUserCommand command = RegisterInternalUserCommand.builder()
                .email("admin@example.com")
                .phoneNumber("+905559876543")
                .password("Password123!")
                .roleIds(List.of(roleId))
                .build();

        Role adminRole = mock(Role.class);
        List<Role> roles = List.of(adminRole);
        User tempUser = User.builder().build();
        tempUser.setId(new UserId(UUID.randomUUID())); 
        String externalId = UUID.randomUUID().toString();
        CreateUserResponse response = CreateUserResponse.builder().message("Internal user created successfully").build();

        doNothing().when(userCreateHelper).checkUserUniqueness(command.getEmail(), command.getPhoneNumber());
        when(userRepository.findRolesByIds(anyList())).thenReturn(roles);
        when(userDataMapper.registerInternalUserCommandToUser(command, roles)).thenReturn(tempUser);
        doNothing().when(identityDomainService).initiateInternalUser(tempUser);
        when(identityProviderPort.registerUser(tempUser, command.getPassword())).thenReturn(externalId);
        when(userRepository.save(any(User.class))).thenReturn(tempUser); 
        when(userDataMapper.userToCreateUserResponse(any(User.class), eq("Internal user created successfully"))).thenReturn(response);

        // Act
        CreateUserResponse result = handler.registerInternalUser(command);

        // Assert
        assertNotNull(result);
        assertEquals(response.getMessage(), result.getMessage());

        verify(userCreateHelper).checkUserUniqueness(command.getEmail(), command.getPhoneNumber());
        verify(userRepository).findRolesByIds(anyList());
        verify(userDataMapper).registerInternalUserCommandToUser(command, roles);
        verify(identityDomainService).initiateInternalUser(tempUser);
        verify(identityProviderPort).registerUser(tempUser, command.getPassword());
        verify(userRepository).save(any(User.class));
        verify(userDataMapper).userToCreateUserResponse(any(User.class), eq("Internal user created successfully"));
    }

    @Test
    @DisplayName("Hata Senaryosu (Average Case): İstenen rollerden bazılarının veritabanında bulunamaması durumunda işlemin kesilmesi")
    void registerInternalUser_ShouldThrowException_WhenRoleNotFound_AverageCase() {
        // Arrange
        UUID roleId1 = UUID.randomUUID();
        UUID roleId2 = UUID.randomUUID();
        RegisterInternalUserCommand command = RegisterInternalUserCommand.builder()
                .email("admin@example.com")
                .phoneNumber("+905559876543")
                .roleIds(List.of(roleId1, roleId2))
                .build();

        // Returning only 1 role instead of 2 requested
        List<Role> roles = List.of(mock(Role.class));

        doNothing().when(userCreateHelper).checkUserUniqueness(command.getEmail(), command.getPhoneNumber());
        when(userRepository.findRolesByIds(anyList())).thenReturn(roles);

        // Act & Assert
        IdentityDomainException exception = assertThrows(IdentityDomainException.class, () -> handler.registerInternalUser(command));
        assertEquals("Some roles could not be found!", exception.getMessage());

        verify(userDataMapper, never()).registerInternalUserCommandToUser(any(), anyList());
        verify(identityProviderPort, never()).registerUser(any(), any());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Hata Senaryosu (Edge Case): İç personel kaydı sırasında benzersizlik kuralı ihlalinde işlemin anında iptali")
    void registerInternalUser_ShouldThrowException_WhenUniquenessFails_EdgeCase() {
        // Arrange
        RegisterInternalUserCommand command = RegisterInternalUserCommand.builder()
                .email("duplicate_admin@example.com")
                .phoneNumber("+905559876543")
                .roleIds(List.of(UUID.randomUUID()))
                .build();

        doThrow(new IdentityDomainException("User with email duplicate_admin@example.com already exists!"))
                .when(userCreateHelper).checkUserUniqueness(command.getEmail(), command.getPhoneNumber());

        // Act & Assert
        IdentityDomainException exception = assertThrows(IdentityDomainException.class, () -> handler.registerInternalUser(command));
        assertEquals("User with email duplicate_admin@example.com already exists!", exception.getMessage());

        verify(userRepository, never()).findRolesByIds(anyList());
    }

    @Test
    @DisplayName("Kritik Hata Senaryosu (Worst Case 1): Personel Keycloak kaydı hataya düşerse DB'ye yazmadan sürecin durdurulması")
    void registerInternalUser_ShouldThrowException_WhenKeycloakRegistrationFails_WorstCase1() {
        // Arrange
        RegisterInternalUserCommand command = RegisterInternalUserCommand.builder()
                .email("admin@example.com")
                .phoneNumber("+905559876543")
                .password("Password123!")
                .roleIds(List.of(UUID.randomUUID()))
                .build();

        List<Role> roles = List.of(mock(Role.class));
        User tempUser = User.builder().build();

        doNothing().when(userCreateHelper).checkUserUniqueness(command.getEmail(), command.getPhoneNumber());
        when(userRepository.findRolesByIds(anyList())).thenReturn(roles);
        when(userDataMapper.registerInternalUserCommandToUser(command, roles)).thenReturn(tempUser);
        doNothing().when(identityDomainService).initiateInternalUser(tempUser);
        
        doThrow(new RuntimeException("Keycloak connection refused!"))
                .when(identityProviderPort).registerUser(tempUser, command.getPassword());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> handler.registerInternalUser(command));
        assertEquals("Keycloak connection refused!", exception.getMessage());

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Kritik Hata Senaryosu (Worst Case 2): Personelin DB kaydı sırasında alınan bir istisnada, Keycloak'ta açılmış hesabın derhal geri alınması (Rollback)")
    void registerInternalUser_ShouldRollbackKeycloak_WhenDbSaveFails_WorstCase2() {
        // Arrange
        RegisterInternalUserCommand command = RegisterInternalUserCommand.builder()
                .email("admin@example.com")
                .phoneNumber("+905559876543")
                .password("Password123!")
                .roleIds(List.of(UUID.randomUUID()))
                .build();

        List<Role> roles = List.of(mock(Role.class));
        User tempUser = User.builder().build();
        tempUser.setId(new UserId(UUID.randomUUID())); 
        String externalId = UUID.randomUUID().toString();

        doNothing().when(userCreateHelper).checkUserUniqueness(command.getEmail(), command.getPhoneNumber());
        when(userRepository.findRolesByIds(anyList())).thenReturn(roles);
        when(userDataMapper.registerInternalUserCommandToUser(command, roles)).thenReturn(tempUser);
        doNothing().when(identityDomainService).initiateInternalUser(tempUser);
        when(identityProviderPort.registerUser(tempUser, command.getPassword())).thenReturn(externalId);
        
        doThrow(new DataAccessException("DB Connection Timeout") {})
                .when(userRepository).save(any(User.class));

        // Act & Assert
        IdentityDomainException exception = assertThrows(IdentityDomainException.class, () -> handler.registerInternalUser(command));
        assertEquals("Registration failed due to internal error! DB Connection Timeout", exception.getMessage());

        // Verifying Rollback Method Call is the most critical check!
        verify(identityProviderPort).deleteUser(externalId);
    }
}
