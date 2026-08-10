package com.berkay.identity.service.infrastructure.keycloak.mapper;

import com.berkay.identity.service.domain.entity.User;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
public class KeycloakMapper {

    public UserRepresentation getUserRepresentation(User user, String password) {
        UserRepresentation userRepresentation = new UserRepresentation();

        // Username olarak email kullanıyoruz
        userRepresentation.setUsername(user.getEmail().getValue());
        userRepresentation.setEmail(user.getEmail().getValue());
        userRepresentation.setFirstName(user.getFirstName().getValue());
        userRepresentation.setLastName(user.getLastName().getValue());
        userRepresentation.setEnabled(true);
        userRepresentation.setEmailVerified(false); // Email onayı için ayrı akış çalışacak

        // Custom Attributes
        // KRİTİK MİMARİ KARAR: Keycloak kendi ID'sini üreteceği için, bizim DB'deki ID'mizi
        // Token'a basabilmesi adına "internal_id" olarak Custom Attribute'lara ekliyoruz.
        userRepresentation.setAttributes(Map.of(
                "internal_id", List.of(user.getId().getValue().toString()),
                "user_type", List.of(user.getUserType().name()),
                "phone_number", List.of(user.getPhoneNumber().getValue()),
                "account_status", List.of(user.getStatus().name()), // Admin panelde durum takibi için
                "role_ids", user.getRoles().stream().map(r -> r.getId().getValue().toString()).collect(java.util.stream.Collectors.toList()),
                "organizational_unit_ids", user.getOrganizationalUnitIds().stream().map(java.util.UUID::toString).collect(java.util.stream.Collectors.toList())
        ));

        // ŞİFRE ATAMASI (Önceki hatam düzeltildi)
        if (password != null && !password.isEmpty()) {
            userRepresentation.setCredentials(Collections.singletonList(createPasswordCredentials(password)));
        }

        return userRepresentation;
    }

    // Şifre credential oluşturucu
    private CredentialRepresentation createPasswordCredentials(String password) {
        CredentialRepresentation passwordCredentials = new CredentialRepresentation();
        passwordCredentials.setTemporary(false); // Kullanıcı ilk girişte şifre değiştirmeye zorlanmaz
        passwordCredentials.setType(CredentialRepresentation.PASSWORD);
        passwordCredentials.setValue(password);
        return passwordCredentials;
    }
}