package com.berkay.identity.service.application.initializer;

import com.berkay.identity.service.application.config.SampleUserProperties;
import com.berkay.identity.service.domain.entity.User;
import com.berkay.identity.service.dto.command.RegisterCustomerCommand;
import com.berkay.identity.service.dto.command.RegisterMerchantCommand;
import com.berkay.identity.service.handler.RegisterCustomerCommandHandler;
import com.berkay.identity.service.handler.RegisterMerchantCommandHandler;
import com.berkay.identity.service.ports.output.repository.UserRepository;
import com.berkay.identity.service.dto.command.CreateUserResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SampleUserInitializerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RegisterCustomerCommandHandler registerCustomerCommandHandler;

    @Mock
    private RegisterMerchantCommandHandler registerMerchantCommandHandler;

    @Mock
    private SampleUserProperties properties;

    @InjectMocks
    private SampleUserInitializer initializer;

    @BeforeEach
    void setUp() {
        SampleUserProperties.SampleUser customer = new SampleUserProperties.SampleUser();
        customer.setEmail("customer@test.com");
        customer.setPassword("Customer123!");

        SampleUserProperties.SampleUser merchant = new SampleUserProperties.SampleUser();
        merchant.setEmail("merchant@test.com");
        merchant.setPassword("Merchant123!");

        when(properties.getCustomer()).thenReturn(customer);
        when(properties.getMerchant()).thenReturn(merchant);
    }

    @Test
    @DisplayName("Başarılı Senaryo: Veritabanında sample kullanıcılar yoksa oluşturulmalıdır.")
    void shouldCreateSampleUsers_WhenNotExists() {
        when(userRepository.findByEmail("customer@test.com")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("merchant@test.com")).thenReturn(Optional.empty());

        initializer.run();

        verify(registerCustomerCommandHandler).registerCustomer(any(RegisterCustomerCommand.class));
        verify(registerMerchantCommandHandler).registerMerchant(any(RegisterMerchantCommand.class));
    }

    @Test
    @DisplayName("Alternatif Senaryo: Kullanıcılar varsa oluşturma işlemi atlanmalıdır (Skip).")
    void shouldSkipCreation_WhenUsersAlreadyExist() {
        when(userRepository.findByEmail("customer@test.com")).thenReturn(Optional.of(mock(User.class)));
        when(userRepository.findByEmail("merchant@test.com")).thenReturn(Optional.of(mock(User.class)));

        initializer.run();

        verify(registerCustomerCommandHandler, never()).registerCustomer(any());
        verify(registerMerchantCommandHandler, never()).registerMerchant(any());
    }

    @Test
    @DisplayName("Kısmi Senaryo: Sadece müşteri varsa, sadece satıcı oluşturulmalıdır.")
    void shouldCreateOnlyMerchant_WhenCustomerExists() {
        when(userRepository.findByEmail("customer@test.com")).thenReturn(Optional.of(mock(User.class)));
        when(userRepository.findByEmail("merchant@test.com")).thenReturn(Optional.empty());

        initializer.run();

        verify(registerCustomerCommandHandler, never()).registerCustomer(any());
        verify(registerMerchantCommandHandler).registerMerchant(any(RegisterMerchantCommand.class));
    }
}
