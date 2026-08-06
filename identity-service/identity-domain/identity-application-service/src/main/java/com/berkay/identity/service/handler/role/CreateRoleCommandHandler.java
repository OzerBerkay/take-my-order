package com.berkay.identity.service.handler.role;

import com.berkay.identity.service.domain.IdentityDomainService;
import com.berkay.identity.service.domain.entity.Permission;
import com.berkay.identity.service.domain.entity.Role;
import com.berkay.identity.service.domain.event.RoleCreatedEvent;
import com.berkay.identity.service.domain.exception.IdentityDomainException;
import com.berkay.identity.service.domain.valueobject.DomainType;
import com.berkay.identity.service.domain.valueobject.UserId;
import com.berkay.identity.service.domain.valueobject.UserType;
import com.berkay.identity.service.dto.command.role.CreateRoleCommand;
import com.berkay.identity.service.dto.command.role.CreateRoleResponse;
import com.berkay.identity.service.mapper.RoleDataMapper;
import com.berkay.identity.service.outbox.helper.RoleOutboxHelper;
import com.berkay.identity.service.outbox.model.role.RoleEventPayload;
import com.berkay.identity.service.ports.output.config.RoleSecurityPolicyPort;
import com.berkay.identity.service.ports.output.repository.PermissionRepository;
import com.berkay.identity.service.ports.output.repository.RoleRepository;
import com.berkay.identity.service.ports.output.security.SecurityContextPort;
// import com.berkay.identity.service.outbox.scheduler.RoleOutboxHelper; // TODO: Outbox sınıfı yazılınca açılacak

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class CreateRoleCommandHandler {

    private final IdentityDomainService identityDomainService;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final SecurityContextPort securityContextPort;
    private final RoleSecurityPolicyPort roleSecurityPolicyPort;
    private final RoleOutboxHelper roleOutboxHelper;
    private final RoleDataMapper roleDataMapper;

    @Transactional
    public CreateRoleResponse createRole(CreateRoleCommand command) {
        // 1. İsteği atan (Caller) bilgilerini al
        UserType callerUserType = securityContextPort.getCurrentUserType();
        UserId callerUserId = new UserId(securityContextPort.getCurrentInternalUserId());

        // 2. Caller UserType ile Hedef organizationalUnitId'nin mantıksal uyumu kontrolü
        // Örn: CUSTOMER sadece null organizationalUnitId yaratabilir (ki CUSTOMER yaratamaz gerçi).
        validateUserTypeAndOrganizationalUnitIdLogic(callerUserType, command.getOrganizationalUnitId());

        // 3. INTERNAL ise tüm contextlere erişebilir, MERCHANT ise sadece Security Context'teki kendi ID'sine erişebilir.
        java.util.Set<java.util.UUID> allowedOrganizationalUnitIds = securityContextPort.getAllowedOrganizationalUnitIds();
        if (command.getOrganizationalUnitId() != null && !allowedOrganizationalUnitIds.contains(command.getOrganizationalUnitId())) {
            throw new IdentityDomainException("Spoofing detected! The provided organizationalUnitId does not match your allowed contexts!");
        }

        // 4. Input Validations
        if (command.getName() == null || command.getName().trim().isEmpty()) {
            throw new IdentityDomainException("Role name cannot be empty!");
        }

        if (command.getPermissionIds() == null || command.getPermissionIds().isEmpty()) {
            throw new IdentityDomainException("A role must have at least one permission!");
        }

        // 5. RoleName DB'de var mı kontrolü (Aynı context içinde)
        if (roleRepository.existsByNameAndOrganizationalUnitId(command.getName(), command.getOrganizationalUnitId())) {
            throw new IdentityDomainException("Role with name " + command.getName() + " already exists in context " + command.getOrganizationalUnitId());
        }

        // 6. Permission'ları DB'den Çek (is_active=true olanları Repository halleder)
        List<Permission> permissions = permissionRepository.findActivePermissionsByIds(command.getPermissionIds());
        if (permissions.size() != command.getPermissionIds().size()) {
            throw new IdentityDomainException("Some permissions are invalid, inactive, or do not exist in the system!");
        }

        boolean hasRestricted = permissions.stream().anyMatch(Permission::isRestricted);
        if (hasRestricted) {
            throw new IdentityDomainException("Cannot assign restricted permissions to a custom role!");
        }

        // 5. Caller'ın YAML'daki Allowed Domain listesini çek
        List<DomainType> allowedDomains = roleSecurityPolicyPort.getAllowedDomainsForUserType(callerUserType);

        // 6. Role Nesnesini Builder ile Oluştur (Id'si, başlangıç version'u ve tarihleri initializeRole'de atanacak)
        Role role = Role.builder()
                .name(command.getName())
                .userType(callerUserType) // Rolün tipi, oluşturanın tipinden derive edilir (Döküman 3.c)
                .organizationalUnitId(command.getOrganizationalUnitId())
                .isStatic(false) // Kullanıcının oluşturduğu roller statik olamaz
                .createdByUserId(callerUserId)
                .permissions(permissions)
                .build();

        // 7. Caller'ın kendi yetkilerini çek (Alt küme kuralı için)
        List<Permission> callerPermissions = permissionRepository.findActivePermissionsByRoleIds(securityContextPort.getCurrentUserRoleIds());

        // 8. Domain Service'i çağırırız. (Dönen event nesnesini sadece domain kuralı çalışsın diye kullanıyoruz)
        RoleCreatedEvent event = identityDomainService.validateAndInitiateRoleCreate(role, callerPermissions, allowedDomains);

        // 8. Önce Veritabanına Kaydet (Versiyon atansın)
        Role savedRole = roleRepository.save(role);

        // 9. Mapper'a sadece savedRole nesnesini vererek payload oluştur
        RoleEventPayload eventPayload = roleDataMapper.roleCreatedEventToRoleEventPayload(savedRole);

        // 9. Outbox'a Yaz (Döküman Madde 6)
        roleOutboxHelper.saveRoleOutboxMessage(eventPayload);
        log.info("Role created successfully with ID: {}", savedRole.getId().getValue());

        // 10. Yanıt Dön
        return CreateRoleResponse.builder()
                .roleId(savedRole.getId().getValue())
                .build();
    }

    /**
     * İsteği atan kişinin (Caller), hedef organizationalUnitId üzerinde işlem yapmaya yetkisi var mı?
     */
    private void validateUserTypeAndOrganizationalUnitIdLogic(UserType callerUserType, UUID targetOrganizationalUnitId) {
        if (callerUserType == UserType.INTERNAL) {
            // INTERNAL cannot specify a target context! They only manage global roles.
            if (targetOrganizationalUnitId != null) {
                throw new IdentityDomainException("Internal users can only manage global roles (organizationalUnitId must be null)!");
            }
        } else if (callerUserType == UserType.MERCHANT) {
            // MERCHANT must ALWAYS specify a context (their restaurant).
            if (targetOrganizationalUnitId == null) {
                throw new IdentityDomainException("Merchant users must specify an organizationalUnitId for the role!");
            }
        } else if (callerUserType == UserType.CUSTOMER) {
            throw new IdentityDomainException("Customers are not allowed to create roles.");
        }
    }
}