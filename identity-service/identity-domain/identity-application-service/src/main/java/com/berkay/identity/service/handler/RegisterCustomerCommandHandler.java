package com.berkay.identity.service.handler;

import com.berkay.identity.service.domain.IdentityDomainService;
import com.berkay.identity.service.domain.constants.RoleConstants;
import com.berkay.identity.service.domain.entity.Role;
import com.berkay.identity.service.domain.entity.User;
import com.berkay.identity.service.domain.event.UserCreatedEvent;
import com.berkay.identity.service.domain.exception.IdentityDomainException;
import com.berkay.identity.service.dto.command.CreateUserResponse;
import com.berkay.identity.service.dto.command.RegisterCustomerCommand;
import com.berkay.identity.service.handler.helper.UserCreateHelper;
import com.berkay.identity.service.mapper.UserDataMapper;
import com.berkay.identity.service.outbox.model.DomainEventType;
import com.berkay.identity.service.outbox.model.UserEventPayload;
import com.berkay.identity.service.outbox.scheduler.UserOutboxHelper;
import com.berkay.identity.service.ports.output.repository.IdentityProviderPort;
import com.berkay.identity.service.ports.output.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class RegisterCustomerCommandHandler {

    private final IdentityDomainService identityDomainService;
    private final UserRepository userRepository;
    private final UserDataMapper userDataMapper;
    private final IdentityProviderPort identityProviderPort;
    private final UserOutboxHelper userOutboxHelper;
    private final UserCreateHelper userCreateHelper;

    @Transactional
    public CreateUserResponse registerCustomer(RegisterCustomerCommand command) {
        userCreateHelper.checkUserUniqueness(command.getEmail(), command.getPhoneNumber());

        // Rolü Name ile Bul ve Ata (Application service'te atanıyor çünkü Rolün önce DB'den çekilmesi gerek)
        Role customerRole = userRepository.findRoleByName(RoleConstants.ROLE_CUSTOMER)
                .orElseThrow(() -> new IdentityDomainException("Role not found: " + RoleConstants.ROLE_CUSTOMER));

        User user = userDataMapper.registerCustomerCommandToUser(command, customerRole);

        // Domain Service (Business Logic + Initiate)
        // Customer olduğu için initiateCustomer çalışır
        UserCreatedEvent userCreatedEvent = identityDomainService.initiateCustomer(user);

        // Keycloak'a Kayıt (Bizim ID ile)
        // User nesnesinin ID'sini alıp Keycloak'a o ID ile kaydeder.
        identityProviderPort.registerUser(user, command.getPassword());

        // DB Kayıt
        userRepository.save(user);

        log.info("Saving Outbox Message for user id: {}", user.getId().getValue());

        UserEventPayload payload = userDataMapper.userCreatedEventToUserEventPayload(userCreatedEvent);
        userOutboxHelper.saveUserOutboxMessage(payload, DomainEventType.USER_CREATED);

        log.info("Customer registered successfully with id: {}", user.getId().getValue());
        return userDataMapper.userToCreateUserResponse(user, "Customer registered successfully. Please verify your email/phone.");
    }
}