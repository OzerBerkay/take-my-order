package com.berkay.identity.service.ports.input.service;

import com.berkay.identity.service.dto.command.LoginCommand;
import com.berkay.identity.service.dto.command.RefreshTokenCommand;
import com.berkay.identity.service.dto.command.UpdatePasswordCommand;
import com.berkay.identity.service.dto.command.TokenResponse;

public interface AuthApplicationService {
    TokenResponse login(LoginCommand command);
    TokenResponse refreshToken(RefreshTokenCommand command);
    void updatePassword(UpdatePasswordCommand command);
}
