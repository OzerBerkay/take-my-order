package com.berkay.identity.service;

import com.berkay.identity.service.dto.command.*;
import com.berkay.identity.service.handler.RegisterInternalUserCommandHandler;
import com.berkay.identity.service.handler.RegisterCustomerCommandHandler;
import com.berkay.identity.service.handler.RegisterMerchantCommandHandler;
import com.berkay.identity.service.ports.input.service.UserApplicationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Slf4j
@Validated
@Service
@RequiredArgsConstructor
public class UserApplicationServiceImpl implements UserApplicationService {

    private final RegisterCustomerCommandHandler registerCustomerCommandHandler;
    private final RegisterMerchantCommandHandler registerMerchantCommandHandler;
    private final RegisterInternalUserCommandHandler registerInternalUserCommandHandler;

    @Override
    public CreateUserResponse registerCustomer(RegisterCustomerCommand command) {
        return registerCustomerCommandHandler.registerCustomer(command);
    }

    @Override
    public CreateUserResponse registerMerchant(RegisterMerchantCommand command) {
        return registerMerchantCommandHandler.registerMerchant(command);
    }

    @Override
    public CreateUserResponse registerInternalUser(RegisterInternalUserCommand command) {
        return registerInternalUserCommandHandler.registerInternalUser(command);
    }
}