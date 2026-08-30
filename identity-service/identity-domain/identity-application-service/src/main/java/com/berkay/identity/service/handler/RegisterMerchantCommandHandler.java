package com.berkay.identity.service.handler;

import com.berkay.identity.service.domain.IdentityDomainService;
import com.berkay.identity.service.domain.constants.RoleConstants;
import com.berkay.identity.service.domain.entity.Role;
import com.berkay.identity.service.domain.entity.User;
import com.berkay.identity.service.domain.exception.IdentityDomainException;
import com.berkay.identity.service.dto.command.CreateUserResponse;
import com.berkay.identity.service.dto.command.RegisterMerchantCommand;
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
public class RegisterMerchantCommandHandler {

    private final IdentityDomainService identityDomainService;
    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
    private final UserDataMapper userDataMapper;
    private final IdentityProviderPort identityProviderPort;
    private final UserCreateHelper userCreateHelper;

    @Transactional
    public CreateUserResponse registerMerchant(RegisterMerchantCommand command) {
        userCreateHelper.checkUserUniqueness(command.getEmail(), command.getPhoneNumber());

        // Rolü Name ile Bul ve Ata (Application service'te atanıyor çünkü Rolün önce DB'den çekilmesi gerek)
        Role merchantRole = userRepository.findRoleByName(RoleConstants.MERCHANT_BASE)
                .orElseThrow(() -> new IllegalStateException("Role not found: " + RoleConstants.MERCHANT_BASE));

        // Temp User (No External ID)
        User tempUser = userDataMapper.registerMerchantCommandToUser(command, merchantRole);

        // Domain Service (Business Logic + Initiate)
        // Merchant olduğu için initiateMerchant çalışır, ID ve Statü burada atanır
        identityDomainService.initiateMerchant(tempUser);

        // Keycloak Call -> Get External ID
        String externalId = identityProviderPort.registerUser(tempUser, command.getPassword());

        // Final User (With External ID)
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
            throw new IllegalStateException("Registration failed due to internal error! " + e.getMessage(), e);
        }

        log.info("Merchant registered successfully with id: {}", finalUser.getId().getValue());
        return userDataMapper.userToCreateUserResponse(finalUser, "Merchant registered successfully. Please verify details to create restaurant.");
    }
}