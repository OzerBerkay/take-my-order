package com.berkay.customer.service;

import com.berkay.customer.service.create.CreateCustomerCommand;
import com.berkay.customer.service.domain.CustomerDomainService;
import com.berkay.customer.service.domain.entity.Customer;
import com.berkay.customer.service.domain.event.CustomerCreatedEvent;
import com.berkay.customer.service.domain.exception.CustomerDomainException;
import com.berkay.customer.service.mapper.CustomerDataMapper;
import com.berkay.customer.service.outbox.model.CustomerEventPayload;
import com.berkay.customer.service.outbox.scheduler.CustomerOutboxHelper;
import com.berkay.customer.service.ports.output.repository.CustomerRepository;
import com.berkay.outbox.OutboxStatus;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Component
class CustomerCreateCommandHandler {

    private final CustomerDomainService customerDomainService;
    private final CustomerRepository customerRepository;
    private final CustomerDataMapper customerDataMapper;
    private final CustomerOutboxHelper customerOutboxHelper;
    private final Keycloak keycloak;

    @Value("${keycloak.realm}")
    private String realm; // take-my-order

    public CustomerCreateCommandHandler(CustomerDomainService customerDomainService,
                                        CustomerRepository customerRepository,
                                        CustomerDataMapper customerDataMapper,
                                        CustomerOutboxHelper customerOutboxHelper,
                                        Keycloak keycloak) {
        this.customerDomainService = customerDomainService;
        this.customerRepository = customerRepository;
        this.customerDataMapper = customerDataMapper;
        this.customerOutboxHelper = customerOutboxHelper;
        this.keycloak = keycloak;
    }

    @Transactional
    public CustomerCreatedEvent createCustomer(CreateCustomerCommand createCustomerCommand) {
        // Önce Email Veritabanında Var mı Kontrolü
        if (customerRepository.findCustomerByEmail(createCustomerCommand.getEmail()).isPresent()) {
            log.warn("Could not create customer. Email already exists: {}", createCustomerCommand.getEmail());
            throw new CustomerDomainException("Could not create customer. Email already exists: " + createCustomerCommand.getEmail());
        }

        // KEYCLOAK'TA KULLANICI OLUŞTUR
        String keycloakUserId = createKeycloakUser(createCustomerCommand);

        Customer customer = customerDataMapper.createCustomerCommandToCustomer(createCustomerCommand, keycloakUserId);
        CustomerCreatedEvent customerCreatedEvent = customerDomainService.validateAndInitiateCustomer(customer);

        Customer savedCustomer = customerRepository.createCustomer(customer);
        if (savedCustomer == null) {
            log.error("Could not save customer with id: {}", keycloakUserId);
            throw new CustomerDomainException("Could not save customer with id " + keycloakUserId);
        }

        // Artık Mapper'ı ve Repository'i çağırmıyoruz.
        // Kirli işlerin hepsini Helper'a devrettik.
        log.info("Saving Outbox Message for customer id: {}", keycloakUserId);

        // 1. Önce Event'i Payload DTO'suna çeviriyoruz
        CustomerEventPayload customerEventPayload = customerDataMapper.customerCreatedEventToCustomerEventPayload(customerCreatedEvent);

        // 2. Helper'a Payload veriyoruz (ve başlangıç statüsünü STARTED olarak belirtiyoruz)
        customerOutboxHelper.saveCustomerOutboxMessage(customerEventPayload, OutboxStatus.STARTED);

        log.info("Returning CustomerCreatedEvent for customer id: {}", keycloakUserId);
        return customerCreatedEvent;
    }

    private String createKeycloakUser(CreateCustomerCommand command) {
        UserRepresentation user = getUserRepresentation(command);

        // Keycloak'a Gönder
        //TODO: burada saga geliştirmesi gerekli çünkü bu aşamada bir problem olursa, keycloak'a kaydedilmiş ama veritabanında olmayan bir kullanıcı yaratmış olunur.
        UsersResource usersResource = keycloak.realm(realm).users();
        Response response = usersResource.create(user);

        if (response.getStatus() != 201) {
            log.error("Error creating user in Keycloak. Status: {}", response.getStatus());
            throw new CustomerDomainException("Could not create user in Keycloak! Status: " + response.getStatus());
        }

        // Oluşan ID'yi al (Location header'ından döner: .../users/{id})
        String path = response.getLocation().getPath();
        return path.substring(path.lastIndexOf("/") + 1);
    }

    private static UserRepresentation getUserRepresentation(CreateCustomerCommand command) {
        UserRepresentation user = new UserRepresentation();
        user.setUsername(command.getUsername());
        user.setEmail(command.getEmail());
        user.setFirstName(command.getFirstName());
        user.setLastName(command.getLastName());
        user.setEnabled(true);

        // Şifre Ayarla
        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(command.getPassword());
        credential.setTemporary(false);
        user.setCredentials(List.of(credential));
        return user;
    }
}
