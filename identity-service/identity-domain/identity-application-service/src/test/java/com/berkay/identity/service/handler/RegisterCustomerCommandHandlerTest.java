package com.berkay.identity.service.handler;

import com.berkay.identity.service.domain.IdentityDomainService;
import com.berkay.identity.service.domain.constants.RoleConstants;
import com.berkay.identity.service.domain.entity.Role;
import com.berkay.identity.service.domain.entity.User;
import com.berkay.identity.service.domain.exception.IdentityDomainException;
import com.berkay.identity.service.domain.valueobject.UserId;
import com.berkay.identity.service.dto.command.CreateUserResponse;
import com.berkay.identity.service.dto.command.RegisterCustomerCommand;
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
class RegisterCustomerCommandHandlerTest {

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
    private RegisterCustomerCommandHandler handler;

    @Test
    @DisplayName("Başarılı Senaryo (Best Case): Geçerli verilerle müşteri kaydının başarıyla tamamlanması ve veritabanı ile Keycloak entegrasyonunun hatasız çalışması")
    void registerCustomer_ShouldSucceed_BestCase() {
        // Arrange
        RegisterCustomerCommand command = RegisterCustomerCommand.builder()
                .email("test@example.com")
                .phoneNumber("+905551234567")
                .password("Password123!")
                .build();

        Role customerRole = mock(Role.class);
        User tempUser = User.builder().build();
        tempUser.setId(new UserId(UUID.randomUUID())); // Simulated side-effect of initiateCustomer
        String externalId = UUID.randomUUID().toString();
        CreateUserResponse response = CreateUserResponse.builder().message("Customer registered successfully. Please verify your email/phone.").build();

        doNothing().when(userCreateHelper).checkUserUniqueness(command.getEmail(), command.getPhoneNumber());
        when(userRepository.findRoleByName(RoleConstants.CUSTOMER_BASE)).thenReturn(Optional.of(customerRole));
        when(userDataMapper.registerCustomerCommandToUser(command, customerRole)).thenReturn(tempUser);
        doNothing().when(identityDomainService).initiateCustomer(tempUser);
        when(identityProviderPort.registerUser(tempUser, command.getPassword())).thenReturn(externalId);
        when(userRepository.save(any(User.class))).thenReturn(tempUser); // returning any mock since it's just a save mock
        when(userDataMapper.userToCreateUserResponse(any(User.class), eq("Customer registered successfully. Please verify your email/phone."))).thenReturn(response);

        // Act
        CreateUserResponse result = handler.registerCustomer(command);

        // Assert
        assertNotNull(result);
        assertEquals(response.getMessage(), result.getMessage());

        verify(userCreateHelper).checkUserUniqueness(command.getEmail(), command.getPhoneNumber());
        verify(userRepository).findRoleByName(RoleConstants.CUSTOMER_BASE);
        verify(userDataMapper).registerCustomerCommandToUser(command, customerRole);
        verify(identityDomainService).initiateCustomer(tempUser);
        verify(identityProviderPort).registerUser(tempUser, command.getPassword());
        verify(userRepository).save(any(User.class));
        verify(userDataMapper).userToCreateUserResponse(any(User.class), eq("Customer registered successfully. Please verify your email/phone."));
    }

    @Test
    @DisplayName("Başarılı Senaryo (Adresli): Müşteri kaydı sırasında adres gönderilmişse, adreslerin de kaydedilmesi")
    void registerCustomer_ShouldSucceedAndSaveAddresses_WhenAddressesAreProvided() {
        // Arrange
        CreateAddressCommand addressCommand = CreateAddressCommand.builder()
                .name("Ev Adresi")
                .street("Örnek Sokak No 5")
                .city("İstanbul")
                .postalCode("34000")
                .country("Turkey")
                .build();

        RegisterCustomerCommand command = RegisterCustomerCommand.builder()
                .email("test@example.com")
                .phoneNumber("+905551234567")
                .password("Password123!")
                .addresses(List.of(addressCommand))
                .build();

        Role customerRole = mock(Role.class);
        User tempUser = User.builder().build();
        tempUser.setId(new UserId(UUID.randomUUID()));
        String externalId = UUID.randomUUID().toString();
        CreateUserResponse response = CreateUserResponse.builder().message("Success").build();

        doNothing().when(userCreateHelper).checkUserUniqueness(command.getEmail(), command.getPhoneNumber());
        when(userRepository.findRoleByName(RoleConstants.CUSTOMER_BASE)).thenReturn(Optional.of(customerRole));
        when(userDataMapper.registerCustomerCommandToUser(command, customerRole)).thenReturn(tempUser);
        doNothing().when(identityDomainService).initiateCustomer(tempUser);
        when(identityProviderPort.registerUser(tempUser, command.getPassword())).thenReturn(externalId);
        when(userRepository.save(any(User.class))).thenReturn(tempUser);
        when(userDataMapper.userToCreateUserResponse(any(User.class), any())).thenReturn(response);

        // Act
        CreateUserResponse result = handler.registerCustomer(command);

        // Assert
        assertNotNull(result);
        verify(userRepository).save(any(User.class));
        verify(addressRepository).save(any(Address.class)); // Adresin kaydedildiğini test et
    }

    @Test
    @DisplayName("Hata Senaryosu (Average Case): Müşteri rolünün veritabanında bulunamaması durumunda Keycloak'a gidilmeden işlemin kesilmesi")
    void registerCustomer_ShouldThrowException_WhenRoleNotFound_AverageCase() {
        // Arrange
        RegisterCustomerCommand command = RegisterCustomerCommand.builder()
                .email("test@example.com")
                .phoneNumber("+905551234567")
                .build();

        doNothing().when(userCreateHelper).checkUserUniqueness(command.getEmail(), command.getPhoneNumber());
        when(userRepository.findRoleByName(RoleConstants.CUSTOMER_BASE)).thenReturn(Optional.empty());

        // Act & Assert
        IdentityDomainException exception = assertThrows(IdentityDomainException.class, () -> handler.registerCustomer(command));
        assertEquals("Role not found: " + RoleConstants.CUSTOMER_BASE, exception.getMessage());

        verify(userDataMapper, never()).registerCustomerCommandToUser(any(), any());
        verify(identityProviderPort, never()).registerUser(any(), any());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Hata Senaryosu (Edge Case): E-posta benzersizlik kontrolü başarısız olduğunda işlemin anında kesilmesi")
    void registerCustomer_ShouldThrowException_WhenUniquenessFails_EdgeCase() {
        // Arrange
        RegisterCustomerCommand command = RegisterCustomerCommand.builder()
                .email("duplicate@example.com")
                .phoneNumber("+905551234567")
                .build();

        doThrow(new IdentityDomainException("User with email duplicate@example.com already exists!"))
                .when(userCreateHelper).checkUserUniqueness(command.getEmail(), command.getPhoneNumber());

        // Act & Assert
        IdentityDomainException exception = assertThrows(IdentityDomainException.class, () -> handler.registerCustomer(command));
        assertEquals("User with email duplicate@example.com already exists!", exception.getMessage());

        verify(userRepository, never()).findRoleByName(any());
    }

    @Test
    @DisplayName("Kritik Hata Senaryosu (Worst Case 1): Keycloak kayıt işlemi başarısız olduğunda veritabanı kaydı yapılmadan işlemin iptal edilmesi")
    void registerCustomer_ShouldThrowException_WhenKeycloakRegistrationFails_WorstCase1() {
        // Arrange
        RegisterCustomerCommand command = RegisterCustomerCommand.builder()
                .email("test@example.com")
                .phoneNumber("+905551234567")
                .password("Password123!")
                .build();

        Role customerRole = mock(Role.class);
        User tempUser = User.builder().build();

        doNothing().when(userCreateHelper).checkUserUniqueness(command.getEmail(), command.getPhoneNumber());
        when(userRepository.findRoleByName(RoleConstants.CUSTOMER_BASE)).thenReturn(Optional.of(customerRole));
        when(userDataMapper.registerCustomerCommandToUser(command, customerRole)).thenReturn(tempUser);
        doNothing().when(identityDomainService).initiateCustomer(tempUser);
        
        doThrow(new RuntimeException("Keycloak connection refused!"))
                .when(identityProviderPort).registerUser(tempUser, command.getPassword());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> handler.registerCustomer(command));
        assertEquals("Keycloak connection refused!", exception.getMessage());

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Kritik Hata Senaryosu (Worst Case 2): Veritabanına kayıt sırasında hata oluşursa, Keycloak üzerinde önceden oluşturulmuş kullanıcının silinerek (Rollback) sistem tutarlılığının sağlanması")
    void registerCustomer_ShouldRollbackKeycloak_WhenDbSaveFails_WorstCase2() {
        // Arrange
        RegisterCustomerCommand command = RegisterCustomerCommand.builder()
                .email("test@example.com")
                .phoneNumber("+905551234567")
                .password("Password123!")
                .build();

        Role customerRole = mock(Role.class);
        User tempUser = User.builder().build();
        tempUser.setId(new UserId(UUID.randomUUID())); 
        String externalId = UUID.randomUUID().toString();

        doNothing().when(userCreateHelper).checkUserUniqueness(command.getEmail(), command.getPhoneNumber());
        when(userRepository.findRoleByName(RoleConstants.CUSTOMER_BASE)).thenReturn(Optional.of(customerRole));
        when(userDataMapper.registerCustomerCommandToUser(command, customerRole)).thenReturn(tempUser);
        doNothing().when(identityDomainService).initiateCustomer(tempUser);
        when(identityProviderPort.registerUser(tempUser, command.getPassword())).thenReturn(externalId);
        
        doThrow(new DataAccessException("DB Connection Timeout") {})
                .when(userRepository).save(any(User.class));

        // Act & Assert
        IdentityDomainException exception = assertThrows(IdentityDomainException.class, () -> handler.registerCustomer(command));
        assertEquals("Registration failed due to internal error! DB Connection Timeout", exception.getMessage());

        // Verifying Rollback Method Call is the most critical check!
        verify(identityProviderPort).deleteUser(externalId);
    }
}
