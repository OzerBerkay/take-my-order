package com.berkay.identity.service;

import com.berkay.identity.service.dto.command.LoginCommand;
import com.berkay.identity.service.dto.command.RefreshTokenCommand;
import com.berkay.identity.service.dto.command.TokenResponse;
import com.berkay.identity.service.dto.command.UpdatePasswordCommand;
import com.berkay.identity.service.handler.AuthCommandHandler;
import com.berkay.identity.service.handler.UpdatePasswordCommandHandler;
import com.berkay.identity.service.ports.input.service.AuthApplicationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Slf4j
@Validated
@Service
@RequiredArgsConstructor
public class AuthApplicationServiceImpl implements AuthApplicationService {

    private final AuthCommandHandler authCommandHandler;
    private final UpdatePasswordCommandHandler updatePasswordCommandHandler;

    @Override
    public TokenResponse login(LoginCommand command) {
        return authCommandHandler.login(command);
    }

    @Override
    public TokenResponse refreshToken(RefreshTokenCommand command) {
        return authCommandHandler.refreshToken(command);
    }

    @Override
    public void updatePassword(UpdatePasswordCommand command) {
        updatePasswordCommandHandler.updatePassword(command);
    }
}
