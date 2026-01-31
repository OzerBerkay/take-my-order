package com.berkay.identity.service.domain.entity;

import com.berkay.domain.entity.AggregateRoot;
import com.berkay.identity.service.domain.exception.IdentityDomainException;
import com.berkay.identity.service.domain.valueobject.*;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class User extends AggregateRoot<UserId> {

    // Identity Fields (Value Objects)
    private FirstName firstName;
    private LastName lastName;
    private UserEmail email;
    private PhoneNumber phoneNumber;

    // Business Logic Fields
    private UserType userType;      // CUSTOMER, MERCHANT, INTERNAL
    private AccountStatus status;   // PENDING_APPROVAL, ACTIVE, BLOCKED vs.

    // Security & Access (GÜNCELLENDİ)
    private List<Role> roles;       // Birden fazla rol olabilir (Internal için)

    // Verification Flags (Login olabilir ama onaysız olabilir ayrımı için)
    private boolean isEmailVerified;
    private boolean isPhoneVerified;

    // Profile
    private String imageUrl; // Profil fotosu (Opsiyonel string kalabilir)
    private List<Address> addresses;

    // Audit
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;

    private User(Builder builder) {
        super.setId(builder.userId);
        this.firstName = builder.firstName;
        this.lastName = builder.lastName;
        this.email = builder.email;
        this.phoneNumber = builder.phoneNumber;
        this.userType = builder.userType;
        this.roles = builder.roles;
        this.status = builder.status;
        this.isEmailVerified = builder.isEmailVerified;
        this.isPhoneVerified = builder.isPhoneVerified;
        this.imageUrl = builder.imageUrl;
        this.addresses = builder.addresses;
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

        if (this.addresses == null) {
            this.addresses = new ArrayList<>();
        }
        if (this.roles == null) {
            this.roles = new ArrayList<>();
        }
    }

    private void initializeAudit() {
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("UTC"));
        this.createdAt = now;
        this.updatedAt = now;
    }

    // Müşteri Oluşturma
    public void initializeCustomer() {
        initializeBase();
        this.userType = UserType.CUSTOMER;
        this.status = AccountStatus.PENDING_VERIFICATION;
    }

    // Restoran/Merchant Başvurusu
    public void initializeMerchant() {
        initializeBase();
        this.userType = UserType.MERCHANT;
        this.status = AccountStatus.PENDING_VERIFICATION; // Kritik: Email tel no onayı sonra da Backoffice onayını bekler!
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

        // Kural: Müşterilerin sadece 1 rolü olabilir (ROLE_CUSTOMER)
        if (UserType.CUSTOMER.equals(this.userType) && this.roles.size() > 1) {
            throw new IdentityDomainException("Customers strictly cannot have more than one role!");
        }

        // Kural: Merchant'ların sadece 1 rolü olabilir (ROLE_MERCHANT)
        if (UserType.MERCHANT.equals(this.userType) && this.roles.size() > 1) {
            throw new IdentityDomainException("Merchants strictly cannot have more than one role!");
        }
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
        this.firstName = firstName;
        this.lastName = lastName;
        this.imageUrl = imageUrl;
        updateAudit();
    }

    private void updateAudit() {
        this.updatedAt = ZonedDateTime.now(ZoneId.of("UTC"));
    }

    public FirstName getFirstName() { return firstName; }
    public LastName getLastName() { return lastName; }
    public UserEmail getEmail() { return email; }
    public PhoneNumber getPhoneNumber() { return phoneNumber; }
    public UserType getUserType() { return userType; }
    public List<Role> getRoles() { return roles; }
    public AccountStatus getStatus() { return status; }
    public boolean isEmailVerified() { return isEmailVerified; }
    public boolean isPhoneVerified() { return isPhoneVerified; }
    public String getImageUrl() { return imageUrl; }
    public List<Address> getAddresses() { return addresses; }
    public ZonedDateTime getCreatedAt() { return createdAt; }
    public ZonedDateTime getUpdatedAt() { return updatedAt; }

    public static final class Builder {
        private UserId userId;
        private FirstName firstName;
        private LastName lastName;
        private UserEmail email;
        private PhoneNumber phoneNumber;
        private UserType userType;
        private List<Role> roles;
        private AccountStatus status;
        private boolean isEmailVerified;
        private boolean isPhoneVerified;
        private String imageUrl;
        private List<Address> addresses;
        private ZonedDateTime createdAt;
        private ZonedDateTime updatedAt;

        private Builder() {}

        public static Builder builder() { return new Builder(); }

        public Builder userId(UserId val) { userId = val; return this; }
        public Builder firstName(FirstName val) { firstName = val; return this; }
        public Builder lastName(LastName val) { lastName = val; return this; }
        public Builder email(UserEmail val) { email = val; return this; }
        public Builder phoneNumber(PhoneNumber val) { phoneNumber = val; return this; }
        public Builder userType(UserType val) { userType = val; return this; }
        public Builder roles(List<Role> val) { roles = val; return this; }
        public Builder status(AccountStatus val) { status = val; return this; }
        public Builder isEmailVerified(boolean val) { isEmailVerified = val; return this; }
        public Builder isPhoneVerified(boolean val) { isPhoneVerified = val; return this; }
        public Builder imageUrl(String val) { imageUrl = val; return this; }
        public Builder addresses(List<Address> val) { addresses = val; return this; }
        public Builder createdAt(ZonedDateTime val) { createdAt = val; return this; }
        public Builder updatedAt(ZonedDateTime val) { updatedAt = val; return this; }

        public User build() { return new User(this); }
    }
}