package com.berkay.identity.service.dataaccess.user.entity;

import com.berkay.identity.service.domain.valueobject.AccountStatus;
import com.berkay.identity.service.domain.valueobject.AuthProvider;
import com.berkay.identity.service.domain.valueobject.UserType;
import lombok.*;

import jakarta.persistence.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")
@Entity
public class UserEntity extends BaseEntity {

    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "external_id", nullable = false, unique = true)
    private String externalId; // Keycloak ID

    @Enumerated(EnumType.STRING)
    @Column(name = "auth_provider", nullable = false)
    private AuthProvider authProvider;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "phone_number", nullable = false, unique = true)
    private String phoneNumber;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "is_email_verified", nullable = false)
    private Boolean isEmailVerified;

    @Column(name = "is_phone_verified", nullable = false)
    private Boolean isPhoneVerified;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_type", nullable = false)
    private UserType userType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountStatus status;

    @Version
    private Long version;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_organizational_units", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "organizational_unit_id")
    private java.util.List<UUID> organizationalUnitIds;



    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserEntity that = (UserEntity) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}