package com.berkay.identity.service.handler.helper;

import com.berkay.identity.service.domain.entity.User;
import com.berkay.identity.service.domain.exception.IdentityDomainException;
import com.berkay.identity.service.domain.exception.UserAlreadyExistsException;
import com.berkay.identity.service.ports.output.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserCreateHelperTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserCreateHelper userCreateHelper;

    @Test
    @DisplayName("Başarılı Senaryo (Best Case): Benzersiz e-posta ve telefon numarası ile kayıt doğrulamasından geçilmesi")
    void checkUserUniqueness_ShouldPass_WhenEmailAndPhoneAreUnique() {
        String email = "test@example.com";
        String phone = "+905551234567";

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
        when(userRepository.findByPhoneNumber(phone)).thenReturn(Optional.empty());

        assertDoesNotThrow(() -> userCreateHelper.checkUserUniqueness(email, phone));
    }

    @Test
    @DisplayName("Hata Senaryosu (Edge Case): Veritabanında zaten kayıtlı olan bir e-posta ile kayıt olunmaya çalışıldığında IdentityDomainException fırlatılması")
    void checkUserUniqueness_ShouldThrowException_WhenEmailExists() {
        String email = "existing@example.com";
        String phone = "+905551234567";

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(mock(User.class)));

        UserAlreadyExistsException exception = assertThrows(UserAlreadyExistsException.class, 
            () -> userCreateHelper.checkUserUniqueness(email, phone));

        assertEquals("EMAIL_ALREADY_EXISTS", exception.getErrorCode());
        assertEquals("User with email " + email + " already exists!", exception.getMessage());
    }

    @Test
    @DisplayName("Hata Senaryosu (Edge Case): Veritabanında zaten kayıtlı olan bir telefon numarası ile kayıt olunmaya çalışıldığında IdentityDomainException fırlatılması")
    void checkUserUniqueness_ShouldThrowException_WhenPhoneExists() {
        String email = "test@example.com";
        String phone = "+905551234567";

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
        when(userRepository.findByPhoneNumber(phone)).thenReturn(Optional.of(mock(User.class)));

        UserAlreadyExistsException exception = assertThrows(UserAlreadyExistsException.class, 
            () -> userCreateHelper.checkUserUniqueness(email, phone));

        assertEquals("PHONE_NUMBER_ALREADY_EXISTS", exception.getErrorCode());
        assertEquals("User with phone number " + phone + " already exists!", exception.getMessage());
    }
}
