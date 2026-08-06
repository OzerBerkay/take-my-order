package com.berkay.identity.service.handler;

import com.berkay.identity.service.domain.IdentityDomainService;
import com.berkay.identity.service.domain.constants.RoleConstants;
import com.berkay.identity.service.domain.entity.Role;
import com.berkay.identity.service.domain.entity.User;
import com.berkay.identity.service.domain.exception.IdentityDomainException;
import com.berkay.identity.service.domain.valueobject.UserId;
import com.berkay.identity.service.dto.command.CreateUserResponse;
import com.berkay.identity.service.dto.command.RegisterMerchantCommand;
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

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegisterMerchantCommandHandlerTest {

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
    private RegisterMerchantCommandHandler handler;

    @Test
    @DisplayName("Başarılı Senaryo (Best Case): Geçerli verilerle satıcı (Merchant) kaydının başarıyla tamamlanması")
    void registerMerchant_ShouldSucceed_BestCase() {
        // Arrange
        RegisterMerchantCommand command = RegisterMerchantCommand.builder()
                .email("merchant@example.com")
                .phoneNumber("+905559876543")
                .password("Password123!")
                .build();

        Role merchantRole = mock(Role.class);
        User tempUser = User.builder().build();
        tempUser.setId(new UserId(UUID.randomUUID())); 
        String externalId = UUID.randomUUID().toString();
        CreateUserResponse response = CreateUserResponse.builder().message("Merchant registered successfully. Please verify details to create restaurant.").build();

        doNothing().when(userCreateHelper).checkUserUniqueness(command.getEmail(), command.getPhoneNumber());
        when(userRepository.findRoleByName(RoleConstants.MERCHANT_BASE)).thenReturn(Optional.of(merchantRole));
        when(userDataMapper.registerMerchantCommandToUser(command, merchantRole)).thenReturn(tempUser);
        doNothing().when(identityDomainService).initiateMerchant(tempUser);
        when(identityProviderPort.registerUser(tempUser, command.getPassword())).thenReturn(externalId);
        when(userRepository.save(any(User.class))).thenReturn(tempUser); 
        when(userDataMapper.userToCreateUserResponse(any(User.class), eq("Merchant registered successfully. Please verify details to create restaurant."))).thenReturn(response);

        // Act
        CreateUserResponse result = handler.registerMerchant(command);

        // Assert
        assertNotNull(result);
        assertEquals(response.getMessage(), result.getMessage());

        verify(userCreateHelper).checkUserUniqueness(command.getEmail(), command.getPhoneNumber());
        verify(userRepository).findRoleByName(RoleConstants.MERCHANT_BASE);
        verify(userDataMapper).registerMerchantCommandToUser(command, merchantRole);
        verify(identityDomainService).initiateMerchant(tempUser);
        verify(identityProviderPort).registerUser(tempUser, command.getPassword());
        verify(userRepository).save(any(User.class));
        verify(userDataMapper).userToCreateUserResponse(any(User.class), eq("Merchant registered successfully. Please verify details to create restaurant."));
    }

    @Test
    @DisplayName("Hata Senaryosu (Average Case): Satıcı rolünün (MERCHANT_BASE) veritabanında bulunamaması durumunda işlemin kesilmesi")
    void registerMerchant_ShouldThrowException_WhenRoleNotFound_AverageCase() {
        // Arrange
        RegisterMerchantCommand command = RegisterMerchantCommand.builder()
                .email("merchant@example.com")
                .phoneNumber("+905559876543")
                .build();

        doNothing().when(userCreateHelper).checkUserUniqueness(command.getEmail(), command.getPhoneNumber());
        when(userRepository.findRoleByName(RoleConstants.MERCHANT_BASE)).thenReturn(Optional.empty());

        // Act & Assert
        IdentityDomainException exception = assertThrows(IdentityDomainException.class, () -> handler.registerMerchant(command));
        assertEquals("Role not found: " + RoleConstants.MERCHANT_BASE, exception.getMessage());

        verify(userDataMapper, never()).registerMerchantCommandToUser(any(), any());
        verify(identityProviderPort, never()).registerUser(any(), any());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Hata Senaryosu (Edge Case): Satıcı kaydı sırasında benzersizlik kuralı ihlalinde işlemin iptal edilmesi")
    void registerMerchant_ShouldThrowException_WhenUniquenessFails_EdgeCase() {
        // Arrange
        RegisterMerchantCommand command = RegisterMerchantCommand.builder()
                .email("duplicate_merchant@example.com")
                .phoneNumber("+905559876543")
                .build();

        doThrow(new IdentityDomainException("User with email duplicate_merchant@example.com already exists!"))
                .when(userCreateHelper).checkUserUniqueness(command.getEmail(), command.getPhoneNumber());

        // Act & Assert
        IdentityDomainException exception = assertThrows(IdentityDomainException.class, () -> handler.registerMerchant(command));
        assertEquals("User with email duplicate_merchant@example.com already exists!", exception.getMessage());

        verify(userRepository, never()).findRoleByName(any());
    }

    @Test
    @DisplayName("Kritik Hata Senaryosu (Worst Case 1): Satıcının Keycloak kaydı başarısız olursa sistemin DB kaydı yapmadan işlemi durdurması")
    void registerMerchant_ShouldThrowException_WhenKeycloakRegistrationFails_WorstCase1() {
        // Arrange
        RegisterMerchantCommand command = RegisterMerchantCommand.builder()
                .email("merchant@example.com")
                .phoneNumber("+905559876543")
                .password("Password123!")
                .build();

        Role merchantRole = mock(Role.class);
        User tempUser = User.builder().build();

        doNothing().when(userCreateHelper).checkUserUniqueness(command.getEmail(), command.getPhoneNumber());
        when(userRepository.findRoleByName(RoleConstants.MERCHANT_BASE)).thenReturn(Optional.of(merchantRole));
        when(userDataMapper.registerMerchantCommandToUser(command, merchantRole)).thenReturn(tempUser);
        doNothing().when(identityDomainService).initiateMerchant(tempUser);
        
        doThrow(new RuntimeException("Keycloak connection refused!"))
                .when(identityProviderPort).registerUser(tempUser, command.getPassword());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> handler.registerMerchant(command));
        assertEquals("Keycloak connection refused!", exception.getMessage());

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Kritik Hata Senaryosu (Worst Case 2): Satıcının veritabanı kaydı başarısız olduğunda, Keycloak üzerinde oluşan zombi kaydın silinerek (Rollback) hatanın dışa yansıtılması")
    void registerMerchant_ShouldRollbackKeycloak_WhenDbSaveFails_WorstCase2() {
        // Arrange
        RegisterMerchantCommand command = RegisterMerchantCommand.builder()
                .email("merchant@example.com")
                .phoneNumber("+905559876543")
                .password("Password123!")
                .build();

        Role merchantRole = mock(Role.class);
        User tempUser = User.builder().build();
        tempUser.setId(new UserId(UUID.randomUUID())); 
        String externalId = UUID.randomUUID().toString();

        doNothing().when(userCreateHelper).checkUserUniqueness(command.getEmail(), command.getPhoneNumber());
        when(userRepository.findRoleByName(RoleConstants.MERCHANT_BASE)).thenReturn(Optional.of(merchantRole));
        when(userDataMapper.registerMerchantCommandToUser(command, merchantRole)).thenReturn(tempUser);
        doNothing().when(identityDomainService).initiateMerchant(tempUser);
        when(identityProviderPort.registerUser(tempUser, command.getPassword())).thenReturn(externalId);
        
        doThrow(new DataAccessException("DB Connection Timeout") {})
                .when(userRepository).save(any(User.class));

        // Act & Assert
        IdentityDomainException exception = assertThrows(IdentityDomainException.class, () -> handler.registerMerchant(command));
        assertEquals("Registration failed due to internal error! DB Connection Timeout", exception.getMessage());

        // Verifying Rollback Method Call is the most critical check!
        verify(identityProviderPort).deleteUser(externalId);
    }
}
