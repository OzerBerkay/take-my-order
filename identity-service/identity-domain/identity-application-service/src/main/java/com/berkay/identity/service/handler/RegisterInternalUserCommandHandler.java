package com.berkay.identity.service.handler;

import com.berkay.identity.service.domain.IdentityDomainService;
import com.berkay.identity.service.domain.entity.Role;
import com.berkay.identity.service.domain.entity.User;
import com.berkay.identity.service.domain.event.UserCreatedEvent;
import com.berkay.identity.service.domain.exception.IdentityDomainException;
import com.berkay.identity.service.domain.valueobject.RoleId;
import com.berkay.identity.service.dto.command.RegisterInternalUserCommand;
import com.berkay.identity.service.dto.command.CreateUserResponse;
import com.berkay.identity.service.handler.helper.UserCreateHelper;
import com.berkay.identity.service.mapper.UserDataMapper;
import com.berkay.identity.service.ports.output.repository.IdentityProviderPort;
import com.berkay.identity.service.ports.output.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class RegisterInternalUserCommandHandler {

    private final IdentityDomainService identityDomainService;
    private final UserRepository userRepository;
    private final UserDataMapper userDataMapper;
    private final IdentityProviderPort identityProviderPort;
    private final UserCreateHelper userCreateHelper;

    @Transactional
    public CreateUserResponse registerInternalUser(RegisterInternalUserCommand command) {
        userCreateHelper.checkUserUniqueness(command.getEmail(), command.getPhoneNumber());

        // Rolleri DB'den bul
        List<RoleId> roleIds = command.getRoleIds().stream()
                .map(RoleId::new)
                .collect(Collectors.toList());

        List<Role> roles = userRepository.findRolesByIds(roleIds);

        if (roles.size() != command.getRoleIds().size()) {
            throw new IdentityDomainException("Some roles could not be found!");
        }

        // DTO -> Domain Entity
        User user = userDataMapper.registerInternalUserCommandToUser(command, roles);

        // Domain Service (initiateInternalUser -> ACTIVE)
        UserCreatedEvent userCreatedEvent = identityDomainService.initiateInternalUser(user);

        // Keycloak'ta Kullanıcı Oluştur
        identityProviderPort.createUser(user, command.getPassword());

        // DB Kayıt
        // Outbox gereksiz çünkü diğer servislerde internal kullanıcının hiçbir işi yok.
        userRepository.save(user);

        log.info("Internal user created successfully with id: {}", user.getId().getValue());

        return userDataMapper.userToCreateUserResponse(user, "Internal user created successfully");
    }
}