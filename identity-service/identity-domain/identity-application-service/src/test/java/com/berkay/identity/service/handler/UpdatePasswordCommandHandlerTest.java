package com.berkay.identity.service.handler;

import com.berkay.identity.service.domain.entity.User;
import com.berkay.identity.service.domain.exception.IdentityDomainException;
import com.berkay.identity.service.domain.valueobject.UserId;
import com.berkay.identity.service.dto.command.UpdatePasswordCommand;
import com.berkay.identity.service.ports.output.repository.IdentityProviderPort;
import com.berkay.identity.service.ports.output.repository.UserRepository;
import com.berkay.identity.service.ports.output.security.SecurityContextPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdatePasswordCommandHandlerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private IdentityProviderPort identityProviderPort;

    @Mock
    private SecurityContextPort securityContextPort;

    @InjectMocks
    private UpdatePasswordCommandHandler handler;

    @Test
    @DisplayName("Başarılı Senaryo: Geçerli token ile şifre güncelleme")
    void updatePassword_ShouldSucceed() {
        // Arrange
        UUID currentUserId = UUID.randomUUID();
        String externalId = UUID.randomUUID().toString();
        
        UpdatePasswordCommand command = UpdatePasswordCommand.builder()
                .newPassword("New123!")
                .build();

        User user = User.builder().externalId(externalId).build();
        user.setId(new UserId(currentUserId));

        when(securityContextPort.getCurrentInternalUserId()).thenReturn(currentUserId);
        when(userRepository.findById(new UserId(currentUserId))).thenReturn(Optional.of(user));
        doNothing().when(identityProviderPort).updatePassword(externalId, command.getNewPassword());

        // Act
        handler.updatePassword(command);

        // Assert
        verify(securityContextPort).getCurrentInternalUserId();
        verify(userRepository).findById(new UserId(currentUserId));
        verify(identityProviderPort).updatePassword(externalId, command.getNewPassword());
    }

    @Test
    @DisplayName("Hata Senaryosu: Security context boş veya geçersiz token ise")
    void updatePassword_ShouldThrowException_WhenNoValidToken() {
        // Arrange
        UpdatePasswordCommand command = UpdatePasswordCommand.builder()
                .newPassword("New123!")
                .build();

        when(securityContextPort.getCurrentInternalUserId())
                .thenThrow(new IdentityDomainException("Security context does not contain a valid JWT Token!"));

        // Act & Assert
        IdentityDomainException exception = assertThrows(IdentityDomainException.class, 
                () -> handler.updatePassword(command));
                
        assertEquals("Security context does not contain a valid JWT Token!", exception.getMessage());
        verify(userRepository, never()).findById(any());
        verify(identityProviderPort, never()).updatePassword(any(), any());
    }
}
