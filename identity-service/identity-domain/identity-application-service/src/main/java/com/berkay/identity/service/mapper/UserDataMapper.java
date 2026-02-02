package com.berkay.identity.service.mapper;

import com.berkay.identity.service.domain.entity.Address;
import com.berkay.identity.service.domain.entity.Role;
import com.berkay.identity.service.domain.entity.User;
import com.berkay.identity.service.domain.event.UserCreatedEvent;
import com.berkay.identity.service.domain.valueobject.*;
import com.berkay.identity.service.dto.command.CreateAddressCommand;
import com.berkay.identity.service.dto.command.RegisterCustomerCommand;
import com.berkay.identity.service.dto.command.RegisterMerchantCommand;
import com.berkay.identity.service.dto.command.CreateUserResponse;
import com.berkay.identity.service.outbox.model.UserEventPayload;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class UserDataMapper {

    // Customer Mapping
    public User registerCustomerCommandToUser(RegisterCustomerCommand command, Role role) {
        return User.Builder.builder()
                .email(new UserEmail(command.getEmail()))
                .firstName(new FirstName(command.getFirstName()))
                .lastName(new LastName(command.getLastName()))
                .phoneNumber(new PhoneNumber(command.getPhoneNumber()))
                .addresses(addressCommandsToAddresses(command.getAddresses()))
                .roles(new ArrayList<>(Collections.singletonList(role)))
                // Initialize metodu Type, Status ayarlayacak, o yüzden burada boş bırakıyoruz
                .build();
    }

    // Merchant Mapping
    public User registerMerchantCommandToUser(RegisterMerchantCommand command, Role role) {
        return User.Builder.builder()
                .email(new UserEmail(command.getEmail()))
                .firstName(new FirstName(command.getFirstName()))
                .lastName(new LastName(command.getLastName()))
                .phoneNumber(new PhoneNumber(command.getPhoneNumber()))
                .addresses(addressCommandsToAddresses(command.getAddresses()))
                // new ArrayList<>(...) mutable olması ve gelecekte rol eklenmesi gerekirse patlamaması için gerekli
                // Collections.singletonList(role) immutable çalışmakta
                .roles(new ArrayList<>(Collections.singletonList(role)))
                // Status ve UserType -> initiateMerchant() içinde atanacak.
                .build();
    }

    public CreateUserResponse userToCreateUserResponse(User user, String message) {
        return CreateUserResponse.builder()
                .userId(user.getId().getValue())
                .message(message)
                .build();
    }

    private List<Address> addressCommandsToAddresses(List<CreateAddressCommand> commands) {
        if (commands == null) return Collections.emptyList();
        return commands.stream()
                .map(this::commandToAddress)
                .collect(Collectors.toList());
    }

    private Address commandToAddress(CreateAddressCommand command) {
        return Address.create(
                command.getName(),
                command.getStreet(),
                command.getCity(),
                command.getPostalCode(),
                command.getCountry()
        );
    }

    public UserEventPayload userCreatedEventToUserEventPayload(UserCreatedEvent event) {
        return UserEventPayload.builder()
                .userId(event.getUser().getId().getValue().toString())
                .email(event.getUser().getEmail().getValue())
                .phoneNumber(event.getUser().getPhoneNumber().getValue())
                .firstName(event.getUser().getFirstName().getValue())
                .lastName(event.getUser().getLastName().getValue())
                .userType(event.getUser().getUserType().name())
                .accountStatus(event.getUser().getStatus().name())
                .createdAt(event.getCreatedAt())
                .build();
    }
}