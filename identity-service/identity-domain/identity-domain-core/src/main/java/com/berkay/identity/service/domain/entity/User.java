package com.berkay.identity.service.domain.entity;

import com.berkay.domain.entity.AggregateRoot;
import com.berkay.identity.service.domain.exception.IdentityDomainException;
import com.berkay.identity.service.domain.valueobject.*;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class User extends AggregateRoot<UserId> {

    // Identity Fields (Value Objects)
    private final String externalId;  // Keycloak ID'si (Login işlemleri bunu kullanır)
    private FirstName firstName;
    private LastName lastName;
    private UserEmail email;
    private PhoneNumber phoneNumber;

    // Business Logic Fields
    private UserType userType;      // CUSTOMER, MERCHANT, INTERNAL
    private AccountStatus status;   // PENDING_APPROVAL, ACTIVE, BLOCKED vs.
    private final AuthProvider authProvider;

    // Security & Access (GÜNCELLENDİ)
    private List<Role> roles;       // Birden fazla rol olabilir (Internal için)
    private List<UUID> organizationalUnitIds; // Şube / Org Unit ID'leri

    // Verification Flags (Login olabilir ama onaysız olabilir ayrımı için)
    private boolean isEmailVerified;
    private boolean isPhoneVerified;

    // Profile
    private String imageUrl; // Profil fotosu (Opsiyonel string kalabilir)


    // Audit
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;

    private User(Builder builder) {
        super.setId(builder.userId);
        this.authProvider = builder.authProvider;
        this.externalId = builder.externalId;
        this.firstName = builder.firstName;
        this.lastName = builder.lastName;
        this.email = builder.email;
        this.phoneNumber = builder.phoneNumber;
        this.userType = builder.userType;
        this.roles = builder.roles;
        this.organizationalUnitIds = builder.organizationalUnitIds;
        this.status = builder.status;
        this.isEmailVerified = builder.isEmailVerified;
        this.isPhoneVerified = builder.isPhoneVerified;
        this.imageUrl = builder.imageUrl;
        this.createdAt = builder.createdAt;
        this.updatedAt = builder.updatedAt;
    }

    // Ortak Başlangıç İşlemleri
    private void initializeBase() {
        setId(new UserId(UUID.randomUUID()));
        this.isEmailVerified = false;
        this.isPhoneVerified = false;

        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("UTC"));
        this.createdAt = now;
        this.updatedAt = now;

        if (this.roles == null) {
            this.roles = new ArrayList<>();
        }
        if (this.organizationalUnitIds == null) {
            this.organizationalUnitIds = new ArrayList<>();
        }
    }

    // Müşteri Oluşturma
    public void initializeCustomer() {
        initializeBase();
        this.userType = UserType.CUSTOMER;
        this.status = AccountStatus.ACTIVE;
    }

    // Restoran/Merchant Başvurusu
    public void initializeMerchant() {
        initializeBase();
        this.userType = UserType.MERCHANT;
        this.status = AccountStatus.ACTIVE;
    }

    // İç Personel (Admin/Backoffice) Oluşturma
    public void initializeInternalUser() {
        initializeBase();
        this.userType = UserType.INTERNAL;
        this.status = AccountStatus.ACTIVE; // Admin oluşturduğu için güvenli

        // Internal kullanıcılar için verify edilmiş kabul ediyoruz (initializeBase false atamıştı, eziyoruz)
        this.isEmailVerified = true;
        this.isPhoneVerified = true;
    }

    // Validasyonlar

    public void validateUser() {
        // ID ve VO validasyonları constructor'da yapıldı.
        // Burada Aggregate bütünlüğünü ve İş Kurallarını denetliyoruz.

        if (this.roles == null || this.roles.isEmpty()) {
            throw new IdentityDomainException("User must have at least one role assigned!");
        }

        if (this.email == null && this.phoneNumber == null) {
            throw new IdentityDomainException("User must have either an email or a phone number!");
        }

        // Kural: Müşterilerin sadece 1 rolü olabilir (CUSTOMER_BASE)
        if (UserType.CUSTOMER.equals(this.userType) && this.roles.size() > 1) {
            throw new IdentityDomainException("Customers strictly cannot have more than one role!");
        }

        // Kural: Merchant'ların temel rolü MERCHANT_BASE'dir ancak RESTAURANT_OWNER gibi dinamik roller de alabilirler.
        // Bu yüzden MERCHANT için tek rol kısıtlamasını kaldırıyoruz.
    }

    // State Transitions

    public void verifyEmail() {
        this.isEmailVerified = true;
        updateAudit();
        checkVerificationStatus();
    }

    public void verifyPhoneNumber() {
        this.isPhoneVerified = true;
        updateAudit();
        checkVerificationStatus();
    }

    private void checkVerificationStatus() {
        // Eğer statü zaten PENDING_VERIFICATION değilse (Active, Blocked vs.) işlem yapma.
        if (!AccountStatus.PENDING_VERIFICATION.equals(this.status)) {
            return;
        }

        // İkisi de doğrulanmış mı?
        if (this.isEmailVerified && this.isPhoneVerified) {

            if (UserType.CUSTOMER.equals(this.userType)) {
                // Müşteri ise direkt içeri al.
                this.status = AccountStatus.ACTIVE;
            }
            else if (UserType.MERCHANT.equals(this.userType)) {
                // Merchant ise bir sonraki aşamaya (Admin Onayına) geçir.
                this.status = AccountStatus.PENDING_APPROVAL;
            }
        }
    }

    // Admin Onayı (Backoffice'ten çağrılır)
    public void approveMerchant() {
        if (!UserType.MERCHANT.equals(this.userType)) {
            throw new IdentityDomainException("Only MERCHANTS can be approved!");
        }

        // GÜVENLİK: Eğer adam mailini doğrulamamışsa (PENDING_VERIFICATION), admin onaylayamaz!
        if (AccountStatus.PENDING_VERIFICATION.equals(this.status)) {
            throw new IdentityDomainException("Merchant must verify email and phone before approval!");
        }

        if (AccountStatus.ACTIVE.equals(this.status)) {
            throw new IdentityDomainException("Merchant is already active!");
        }

        // Sadece PENDING_APPROVAL durumundaysa onaylanır.
        if (AccountStatus.PENDING_APPROVAL.equals(this.status)) {
            this.status = AccountStatus.ACTIVE;
            updateAudit();
        }
    }

    public void updateProfile(FirstName firstName, LastName lastName, String imageUrl) {
        if (firstName != null) this.firstName = firstName;
        if (lastName != null) this.lastName = lastName;
        if (imageUrl != null) this.imageUrl = imageUrl;
        updateAudit();
    }

    public void updateStatus(AccountStatus status) {
        if (status == null) {
            throw new IdentityDomainException("Status cannot be null");
        }
        this.status = status;
        updateAudit();
    }

    public void updateOrganizationalUnits(List<UUID> organizationalUnitIds) {
        if (organizationalUnitIds == null) {
            this.organizationalUnitIds = new ArrayList<>();
        } else {
            this.organizationalUnitIds = new ArrayList<>(organizationalUnitIds);
        }
        updateAudit();
    }

    public void addOrganizationalUnit(com.berkay.identity.service.domain.entity.OrganizationalUnit orgUnit) {
        if (orgUnit == null) {
            throw new IdentityDomainException("OrganizationalUnit cannot be null");
        }
        if (this.userType == UserType.MERCHANT && orgUnit.getType() != com.berkay.identity.service.domain.valueobject.OrganizationalUnitType.MERCHANT) {
            throw new IdentityDomainException("Merchant user cannot be assigned to non-merchant organizational unit!");
        }
        if (this.userType == UserType.INTERNAL && orgUnit.getType() != com.berkay.identity.service.domain.valueobject.OrganizationalUnitType.INTERNAL) {
            throw new IdentityDomainException("Internal user cannot be assigned to non-internal organizational unit!");
        }
        if (this.userType == UserType.CUSTOMER) {
            throw new IdentityDomainException("Customer user cannot be assigned to any organizational unit!");
        }
        
        if (this.organizationalUnitIds == null) {
            this.organizationalUnitIds = new ArrayList<>();
        } else {
            this.organizationalUnitIds = new ArrayList<>(this.organizationalUnitIds);
        }
        
        UUID orgUnitId = orgUnit.getId().getValue();
        if (!this.organizationalUnitIds.contains(orgUnitId)) {
            this.organizationalUnitIds.add(orgUnitId);
            updateAudit();
        }
    }

    public void removeOrganizationalUnit(com.berkay.identity.service.domain.entity.OrganizationalUnit orgUnit) {
        if (orgUnit == null) {
            throw new IdentityDomainException("OrganizationalUnit cannot be null");
        }
        if (this.organizationalUnitIds != null) {
            this.organizationalUnitIds = new ArrayList<>(this.organizationalUnitIds);
            this.organizationalUnitIds.remove(orgUnit.getId().getValue());
            updateAudit();
        }
    }

    public void addRole(Role role) {
        if (role == null) {
            throw new IdentityDomainException("Role cannot be null!");
        }
        if (UserType.CUSTOMER.equals(this.userType)) {
            throw new IdentityDomainException("Cannot manually assign roles to CUSTOMER users!");
        }
        if (role.isStatic()) {
            throw new IdentityDomainException("Cannot manually assign static base roles!");
        }

        addRoleInternal(role);
    }

    public void addSystemRole(Role role) {
        if (role == null) {
            throw new IdentityDomainException("Role cannot be null!");
        }
        addRoleInternal(role);
    }

    private void addRoleInternal(Role role) {
        if (this.roles == null) {
            this.roles = new ArrayList<>();
        }

        boolean hasRole = this.roles.stream().anyMatch(r -> r.getId().equals(role.getId()));
        if (!hasRole) {
            this.roles.add(role);
            updateAudit();
        }
    }

    public void removeRole(Role role) {
        if (role == null) {
            throw new IdentityDomainException("Role cannot be null!");
        }
        if (UserType.CUSTOMER.equals(this.userType)) {
            throw new IdentityDomainException("Cannot manually remove roles from CUSTOMER users!");
        }
        if (role.isStatic()) {
            throw new IdentityDomainException("Cannot manually remove static base roles!");
        }

        if (this.roles != null) {
            this.roles.removeIf(r -> r.getId().equals(role.getId()));
            updateAudit();
        }
    }

    private void updateAudit() {
        this.updatedAt = ZonedDateTime.now(ZoneId.of("UTC"));
    }

    public FirstName getFirstName() { return firstName; }
    public String getExternalId() { return externalId; }
    public AuthProvider getAuthProvider() { return authProvider; }
    public LastName getLastName() { return lastName; }
    public UserEmail getEmail() { return email; }
    public PhoneNumber getPhoneNumber() { return phoneNumber; }
    public UserType getUserType() { return userType; }
    public List<Role> getRoles() {
        return this.roles == null ? Collections.emptyList() : Collections.unmodifiableList(this.roles);
    }
    public List<UUID> getOrganizationalUnitIds() {
        return this.organizationalUnitIds == null ? Collections.emptyList() : Collections.unmodifiableList(this.organizationalUnitIds);
    }
    public AccountStatus getStatus() { return status; }
    public boolean isEmailVerified() { return isEmailVerified; }
    public boolean isPhoneVerified() { return isPhoneVerified; }
    public String getImageUrl() { return imageUrl; }
    public ZonedDateTime getCreatedAt() { return createdAt; }
    public ZonedDateTime getUpdatedAt() { return updatedAt; }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private UserId userId;
        private String externalId;
        private AuthProvider authProvider;
        private FirstName firstName;
        private LastName lastName;
        private UserEmail email;
        private PhoneNumber phoneNumber;
        private UserType userType;
        private List<Role> roles;
        private List<UUID> organizationalUnitIds;
        private AccountStatus status;
        private boolean isEmailVerified;
        private boolean isPhoneVerified;
        private String imageUrl;
        private ZonedDateTime createdAt;
        private ZonedDateTime updatedAt;

        private Builder() {}

        public static Builder builder() { return new Builder(); }

        public Builder userId(UserId val) { userId = val; return this; }
        public Builder externalId(String val) { externalId = val; return this; }
        public Builder authProvider(AuthProvider val) { authProvider = val; return this; }
        public Builder firstName(FirstName val) { firstName = val; return this; }
        public Builder lastName(LastName val) { lastName = val; return this; }
        public Builder email(UserEmail val) { email = val; return this; }
        public Builder phoneNumber(PhoneNumber val) { phoneNumber = val; return this; }
        public Builder userType(UserType val) { userType = val; return this; }
        public Builder roles(List<Role> val) { roles = val; return this; }
        public Builder organizationalUnitIds(List<UUID> val) { organizationalUnitIds = val; return this; }
        public Builder status(AccountStatus val) { status = val; return this; }
        public Builder isEmailVerified(boolean val) { isEmailVerified = val; return this; }
        public Builder isPhoneVerified(boolean val) { isPhoneVerified = val; return this; }
        public Builder imageUrl(String val) { imageUrl = val; return this; }
        public Builder createdAt(ZonedDateTime val) { createdAt = val; return this; }
        public Builder updatedAt(ZonedDateTime val) { updatedAt = val; return this; }

        // Copy Method (Handler'da işimize yarayacak)
        public static Builder from(User user) {
            return new Builder()
                    .userId(user.getId())
                    .externalId(user.getExternalId())
                    .authProvider(user.getAuthProvider())
                    .email(user.getEmail())
                    .phoneNumber(user.getPhoneNumber())
                    .firstName(user.getFirstName())
                    .lastName(user.getLastName())
                    .imageUrl(user.getImageUrl())
                    .isEmailVerified(user.isEmailVerified())
                    .isPhoneVerified(user.isPhoneVerified())
                    .userType(user.getUserType())
                    .status(user.getStatus())
                    .roles(user.getRoles())
                    .organizationalUnitIds(user.getOrganizationalUnitIds())
                    .createdAt(user.getCreatedAt())
                    .updatedAt(user.getUpdatedAt());
        }

        public User build() { return new User(this); }
    }
}