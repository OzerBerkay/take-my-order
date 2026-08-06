package com.berkay.identity.service.handler;

import com.berkay.identity.service.domain.IdentityDomainService;
import com.berkay.identity.service.domain.constants.RoleConstants;
import com.berkay.identity.service.domain.entity.Role;
import com.berkay.identity.service.domain.entity.User;
import com.berkay.identity.service.domain.exception.IdentityDomainException;
import com.berkay.identity.service.dto.command.CreateUserResponse;
import com.berkay.identity.service.dto.command.RegisterCustomerCommand;
import com.berkay.identity.service.handler.helper.UserCreateHelper;
import com.berkay.identity.service.mapper.UserDataMapper;
import com.berkay.identity.service.ports.output.repository.AddressRepository;
import com.berkay.identity.service.ports.output.repository.IdentityProviderPort;
import com.berkay.identity.service.ports.output.repository.UserRepository;
import com.berkay.identity.service.dto.command.CreateAddressCommand;
import com.berkay.identity.service.domain.entity.Address;
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
    private final AddressRepository addressRepository;
    private final UserDataMapper userDataMapper;
    private final IdentityProviderPort identityProviderPort;
    private final UserCreateHelper userCreateHelper;

    @Transactional
    public CreateUserResponse registerCustomer(RegisterCustomerCommand command) {
        userCreateHelper.checkUserUniqueness(command.getEmail(), command.getPhoneNumber());

        // Rolü Name ile Bul ve Ata (Application service'te atanıyor çünkü Rolün önce DB'den çekilmesi gerek)
        Role customerRole = userRepository.findRoleByName(RoleConstants.CUSTOMER_BASE)
                .orElseThrow(() -> new IdentityDomainException("Role not found: " + RoleConstants.CUSTOMER_BASE));

        // Create Temp User (Without External ID)
        User tempUser = userDataMapper.registerCustomerCommandToUser(command, customerRole);

        // Domain Service (Business Logic + Initiate)
        // Customer olduğu için initiateCustomer çalışır, ID ve Statü burada atanır
        identityDomainService.initiateCustomer(tempUser);

        // KEYCLOAK REGISTRATION (Returns External ID)
        String externalId = identityProviderPort.registerUser(tempUser, command.getPassword());

        // Create Final User (With External ID)
        // User.Builder.from(...) metodunu Domain'e eklemiştik, onu kullanıyoruz.
        User finalUser = User.Builder.from(tempUser)
                .externalId(externalId)
                .build();

        try {
            // DB Kayıt
            userRepository.save(finalUser);
            
            // Adresleri Kaydet
            if (command.getAddresses() != null && !command.getAddresses().isEmpty()) {
                for (CreateAddressCommand addressCommand : command.getAddresses()) {
                    Address address = Address.create(
                            finalUser.getId(),
                            addressCommand.getName(),
                            addressCommand.getStreet(),
                            addressCommand.getCity(),
                            addressCommand.getPostalCode(),
                            addressCommand.getCountry()
                    );
                    addressRepository.save(address);
                }
            }
        } catch (Exception e) {
            log.error("Failed to save user in DB! Rolling back Keycloak creation for externalId: {}", externalId, e);
            identityProviderPort.deleteUser(externalId);
            throw new IdentityDomainException("Registration failed due to internal error! " + e.getMessage(), e);
        }

        log.info("Customer registered successfully with id: {}", finalUser.getId().getValue());
        return userDataMapper.userToCreateUserResponse(finalUser, "Customer registered successfully. Please verify your email/phone.");
    }
}